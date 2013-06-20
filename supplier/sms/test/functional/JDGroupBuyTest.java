package functional;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.resale.ResalerFactory;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.Order;
import models.order.OrderStatus;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.ResalerProduct;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import play.Logger;
import play.libs.XML;
import play.libs.XPath;
import play.mvc.Http;
import play.templates.Template;
import play.templates.TemplateLoader;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 * Date: 13-2-14
 */
public class JDGroupBuyTest extends FunctionalTest {
    ResalerProduct product;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        Resaler resaler = FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.loginName = Resaler.JD_LOGIN_NAME;
                target.partner = "JD";
            }
        });
        product = FactoryBoy.create(ResalerProduct.class, new BuildCallback<ResalerProduct>() {
            @Override
            public void build(ResalerProduct target) {
                target.partner = OuterOrderPartner.JD;
            }
        });

        //创建可欠款账户
        AccountUtil.getCreditableAccount(resaler.id, AccountType.RESALER);
        ResalerFactory.getYibaiquanResaler(); //必须存在一百券
    }

    @Test
    public void testSendOrder() {
        Template template = TemplateLoader.load("test/data/jd.SendOrderRequest.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("venderTeamId", product.goodsLinkId);
        String requestBody = template.render(params);
        long orderCount = Order.count();
        long couponCount = ECoupon.count();

        Http.Response response = POST("/api/v1/jd/gb/send-order", MULTIPART_FORM_DATA, requestBody);
        assertIsOk(response);
        assertLogicalOk(response);

        assertEquals(orderCount + 1, Order.count());
        assertEquals(couponCount + 2, ECoupon.count());

        OuterOrder outerOrder = OuterOrder.getOuterOrder("2323", OuterOrderPartner.JD);
        assertNotNull(outerOrder);

        assertNotNull(outerOrder.ybqOrder);
        assertEquals(OrderStatus.PAID, outerOrder.ybqOrder.status);

        assertNotNull(ECoupon.find("byOrderAndPartnerAndPartnerCouponId", outerOrder.ybqOrder, ECouponPartner.JD,
                "123").first());
        assertNotNull(ECoupon.find("byOrderAndPartnerAndPartnerCouponId", outerOrder.ybqOrder, ECouponPartner.JD,
                "456").first());
    }

    @Test
    public void testQueryTeamSellCount() {
        Template template = TemplateLoader.load("test/data/jd.QueryTeamSellCountRequest.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("venderTeamId", product.goodsLinkId);
        String requestBody = template.render(params);

        Http.Response response = POST("/api/v1/jd/gb/query-team-sell-count", MULTIPART_FORM_DATA, requestBody);
        assertIsOk(response);
        assertLogicalOk(response);
    }

    @Test
    public void testSendOrderRefund() {
        Order order = FactoryBoy.create(Order.class);

        Template template = TemplateLoader.load("test/data/jd.SendOrderRefundRequest.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("venderOrderId", order.orderNumber);
        String requestBody = template.render(params);

        Http.Response response = POST("/api/v1/jd/gb/send-order-refund", MULTIPART_FORM_DATA, requestBody);
        assertIsOk(response);
        assertLogicalOk(response);
    }

    @Test
    public void testSendSms() {
        final String partnerCouponId = "abc1";
        ECoupon coupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.partner = ECouponPartner.JD;
                target.partnerCouponId = partnerCouponId;
            }
        });

        Template template = TemplateLoader.load("test/data/jd.SendSmsRequest.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("jdCouponId", partnerCouponId);
        params.put("venderCouponId", coupon.eCouponSn);
        String requestBody = template.render(params);

        Http.Response response = POST("/api/v1/jd/gb/send-sms", MULTIPART_FORM_DATA, requestBody);
        assertIsOk(response);
        assertLogicalOk(response);
    }

    private void assertLogicalOk(Http.Response response) {
        String responseContent = getContent(response);
        try {
            Logger.info("Content=%s", responseContent);
            Document resXml = XML.getDocument(responseContent);
            assertEquals("200", XPath.selectText("/*/ResultCode", resXml));
        }catch (Exception e) {
            fail("can not parse the response as xml");
        }

    }
}
