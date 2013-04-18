package functional.order;

import com.uhuila.common.util.RandomNumberUtil;
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
import models.sales.Goods;
import models.sales.ResalerProduct;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.templates.Template;
import play.templates.TemplateLoader;
import play.test.FunctionalTest;
import util.ws.MockWebServiceClient;

import java.util.HashMap;
import java.util.Map;

/**
 * User: tanglq
 * Date: 13-4-17
 * Time: 下午4:31
 */
public class KangouOrderTest extends FunctionalTest {
    ResalerProduct product;
    Goods goods;
    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        FactoryBoy.create(Supplier.class, new BuildCallback<Supplier>() {
            @Override
            public void build(Supplier target) {
                target.domainName = "kangou";
            }
        });
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods target) {
                target.supplierGoodsId = 162l;
            }
        });

        product = FactoryBoy.create(ResalerProduct.class, new BuildCallback<ResalerProduct>() {
            @Override
            public void build(ResalerProduct target) {
                target.partner = OuterOrderPartner.JD;
            }
        });

        Resaler resaler = FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.loginName = Resaler.JD_LOGIN_NAME;
            }
        });
        //创建可欠款账户
        AccountUtil.getCreditableAccount(resaler.id, AccountType.RESALER);
        ResalerFactory.getYibaiquanResaler(); //必须存在一百券
    }

    @Test
    public void test通过京东生成看购网订单() {
        Template template = TemplateLoader.load("test/data/jd.SendOrderRequest.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("venderTeamId", product.goodsLinkId);
        String requestBody = template.render(params);
        long orderCount = Order.count();
        long couponCount = ECoupon.count();

        // 加入看购网券生成响应
        mockGetCardIdResponse("99888871", "8832424323");
        mockGetCardIdResponse("99888872", "8832424323");

        Http.Response response = POST("/api/v1/jd/gb/send-order", MULTIPART_FORM_DATA, requestBody);
        assertIsOk(response);

        assertEquals(orderCount + 1, Order.count());
        assertEquals(couponCount + 2, ECoupon.count());


        OuterOrder outerOrder = OuterOrder.getOuterOrder("2323", OuterOrderPartner.JD);
        assertNotNull(outerOrder);

        assertNotNull(outerOrder.ybqOrder);
        assertEquals(OrderStatus.PAID, outerOrder.ybqOrder.status);

        ECoupon coupon1 = ECoupon.find("byOrderAndPartnerAndPartnerCouponId", outerOrder.ybqOrder, ECouponPartner.JD,
                "123").first();
        ECoupon coupon2 = ECoupon.find("byOrderAndPartnerAndPartnerCouponId", outerOrder.ybqOrder, ECouponPartner.JD,
                "456").first();
        assertEquals("99888871", coupon1.eCouponSn);
        assertEquals("99888872", coupon2.eCouponSn);
    }


    /**
     * 生成GetCardId的Mock响应数据.
     *
     * @param cardId
     * @param cardNumber
     */
    private void mockGetCardIdResponse(String cardId, String cardNumber) {
        Template template = TemplateLoader.load("test/data/kangou/GetCardIdResponse.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("orderNumber", RandomNumberUtil.generateRandomNumber(10));
        params.put("cardId", cardId);
        params.put("cardNumber", cardNumber);
        String responseBody = template.render(params);
        MockWebServiceClient.addMockHttpRequest(200, responseBody);
    }

}
