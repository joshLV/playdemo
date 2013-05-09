package functional;

import com.uhuila.common.util.DateUtil;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.dangdang.groupbuy.DDErrorCode;
import models.dangdang.groupbuy.DDGroupBuyUtil;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.resale.ResalerStatus;
import models.sales.Goods;
import models.sales.ResalerProduct;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.mvc.Http;
import play.test.FunctionalTest;
import util.DateHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-17
 * Time: 下午2:23
 */
public class DDSendMsgApiTest extends FunctionalTest {
    Resaler resaler;
    Order order;
    ECoupon coupon;
    Goods g;
    OuterOrder outerOrder;
    ResalerProduct product;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        resaler = FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.loginName = Resaler.DD_LOGIN_NAME;
                target.status = ResalerStatus.APPROVED;
            }
        });
        AccountUtil.getCreditableAccount(resaler.id, AccountType.RESALER);
        order = FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order target) {
                target.userId = resaler.id;
            }
        });
        product = FactoryBoy.create(ResalerProduct.class, new BuildCallback<ResalerProduct>() {
            @Override
            public void build(ResalerProduct target) {
                target.partner = OuterOrderPartner.DD;
                target.resaler = resaler;
            }
        });
        g = product.goods;
        coupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.order = order;
                target.eCouponSn = "0159300520";
                target.expireAt = DateUtil.getYesterday();
                target.goods = g;
            }
        });

        outerOrder = FactoryBoy.create(OuterOrder.class, new BuildCallback<OuterOrder>() {
            @Override
            public void build(OuterOrder target) {
                target.ybqOrder = order;
                target.partner = OuterOrderPartner.DD;
            }
        });

    }

    @Test
    public void 测试发送短信_正常情况() {
        coupon.expireAt = DateHelper.afterDays(new Date(), 1);
        coupon.save();

        Map<String, String> params = new HashMap<>();
        params.put("call_time", "1348217629");
        String data = "<data>" +
                "<order><order_id><![CDATA[" + outerOrder.orderId + "]]></order_id>" +
                "<ddgid><![CDATA[" + product.goodsLinkId + "]]></ddgid>" +
                "<spgid><![CDATA[" + product.goodsLinkId + "]]></spgid>" +
                "<user_code><![CDATA[159300520]]></user_code>" +
                "<receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel>" +
                "<consume_id><![CDATA[0159300520]]></consume_id></order></data>";
        String sign = DDGroupBuyUtil.sign("send_msg", data, "1348217629");
        params.put("data", data);
        params.put("sign", sign);

        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);

        assertContentType("text/xml", response);
        Logger.info("response=" + getContent(response));
        String desc = (String)renderArgs("desc");
        assertNotNull(desc);
    }

    @Test
    public void 测试发送短信_sign出错() {
        String data = "<data><order>" +
                "<order_id><![CDATA[" + outerOrder.orderId + "]]></order_id>" +
                "<ddgid><![CDATA[1]]></ddgid>" +
                "<spgid><![CDATA[1]]></spgid>" +
                "<user_code><![CDATA[159300520]]></user_code>" +
                "<receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel>" +
                "<consume_id><![CDATA[0159300520]]></consume_id></order></data>";

        Map<String, String> params = new HashMap<>();
        params.put("data", data);
        params.put("call_time", "1348217629");
        params.put("sign", "");
        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);

        params.put("sign", "beefdebebef85f55ecba47d54d8308e8");
        response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        assertContentType("text/xml", response);
        DDErrorCode errorCode = (DDErrorCode) renderArgs("errorCode");
//        assertEquals(DDErrorCode.VERIFY_FAILED, errorCode);
    }


    @Test
    public void 测试发送短信_没找到对应的当当订单() {
        Map<String, String> params = new HashMap<>();
        params.put("call_time", "1348217629");
        String data = "<data><order>" +
                "<order_id><![CDATA[1]]></order_id>" +
                "<ddgid><![CDATA[1]]></ddgid>" +
                "<spgid><![CDATA[1]]></spgid>" +
                "<user_code><![CDATA[159300520]]></user_code>" +
                "<receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel>" +
                "<consume_id><![CDATA[0159300520]]></consume_id></order></data>";
        String sign = DDGroupBuyUtil.sign("send_msg", data, "1348217629");
        params.put("data", data);
        params.put("sign", sign);


        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        assertContentType("text/xml", response);
        DDErrorCode errorCode = (DDErrorCode) renderArgs("errorCode");
        assertEquals(DDErrorCode.ORDER_NOT_EXITED, errorCode);
    }

    @Test
    public void 测试发送短信_当当用户不存在() {
        String data = "<data><order>" +
                "<order_id><![CDATA[" + outerOrder.orderId + "]]></order_id>" +
                "<ddgid><![CDATA[1]]></ddgid>" +
                "<spgid><![CDATA[1]]></spgid>" +
                "<user_code><![CDATA[159300520]]></user_code>" +
                "<receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel>" +
                "<consume_id><![CDATA[0159300520]]></consume_id></order></data>";

        Map<String, String> params = new HashMap<>();
        params.put("call_time", "1348217629");

        if (resaler == null) {
            resaler = FactoryBoy.create(Resaler.class);
        }
        resaler.status = ResalerStatus.FREEZE;
        resaler.save();
        params.put("data", data);
        String sign = DDGroupBuyUtil.sign("send_msg", data, "1348217629");
        params.put("sign", sign);
        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        assertContentType("text/xml", response);
        DDErrorCode errorCode = (DDErrorCode) renderArgs("errorCode");
        assertEquals(DDErrorCode.USER_NOT_EXITED, errorCode);
    }

    @Test
    public void 测试发送短信_没找到对应的订单() {

        Map<String, String> params = new HashMap<>();
        params.put("call_time", "1348217629");

        if (resaler == null) {
            resaler = FactoryBoy.create(Resaler.class);
        }
        resaler.status = ResalerStatus.APPROVED;
        resaler.save();
        order.userId = 9999l;
        order.save();

        String data = "<data><order>" +
                "<order_id><![CDATA[" + outerOrder.orderId + "]]></order_id>" +
                "<ddgid><![CDATA[" + product.goodsLinkId + "]]></ddgid>" +
                "<spgid><![CDATA[" + product.goodsLinkId + "]]></spgid>" +
                "<user_code><![CDATA[159300520]]></user_code>" +
                "<receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel>" +
                "<consume_id><![CDATA[0159300520]]></consume_id></order></data>";

        String sign = DDGroupBuyUtil.sign("send_msg", data, "1348217629");
        params.put("data", data);
        params.put("sign", sign);
        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        assertContentType("text/xml", response);
        DDErrorCode errorCode = (DDErrorCode) renderArgs("errorCode");
        assertEquals(DDErrorCode.ORDER_NOT_EXITED, errorCode);
    }

    @Test
    public void 测试发送短信_没找到对应的券号() {
        String data = "<data><order>" +
                "<order_id><![CDATA[" + outerOrder.orderId + "]]></order_id>" +
                "<ddgid><![CDATA[1]]></ddgid>" +
                "<spgid><![CDATA[1]]></spgid>" +
                "<user_code><![CDATA[159300520]]></user_code>" +
                "<receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel>" +
                "<consume_id><![CDATA[0159300520]]></consume_id></order></data>";
        Map<String, String> params = new HashMap<>();
        params.put("call_time", "1348217629");

        order.orderNumber = "987654321";
        order.save();
        params.put("data", data);
        String sign = DDGroupBuyUtil.sign("send_msg", data, "1348217629");
        params.put("sign", sign);
        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        assertContentType("text/xml", response);
        DDErrorCode errorCode = (DDErrorCode) renderArgs("errorCode");
        assertEquals(DDErrorCode.COUPON_SN_NOT_EXISTED, errorCode);
    }

    @Test
    public void 测试发送短信_券已过期() {

        String data = "<data><order>" +
                "<order_id><![CDATA[" + outerOrder.orderId + "]]></order_id>" +
                "<ddgid><![CDATA[1]]></ddgid>" +
                "<spgid><![CDATA[" + product.goodsLinkId + "]]></spgid>" +
                "<user_code><![CDATA[159300520]]></user_code>" +
                "<receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel>" +
                "<consume_id><![CDATA[0159300520]]></consume_id></order></data>";

        Map<String, String> params = new HashMap<>();
        params.put("call_time", "1348217629");
        params.put("data", data);
        params.put("sign", DDGroupBuyUtil.sign("send_msg", data, "1348217629"));
        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);

        assertContentType("text/xml", response);
        String desc = (String)renderArgs("desc");
        assertNotNull(desc);
    }

    @Test
    public void 测试发送短信_券已退款() {
        coupon.status = ECouponStatus.REFUND;
        coupon.save();

        String data = "<data><order>" +
                "<order_id><![CDATA[" + outerOrder.orderId + "]]></order_id>" +
                "<ddgid><![CDATA[1]]></ddgid>" +
                "<spgid><![CDATA[" + product.goodsLinkId + "]]></spgid>" +
                "<user_code><![CDATA[159300520]]></user_code>" +
                "<receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel>" +
                "<consume_id><![CDATA[0159300520]]></consume_id></order></data>";

        Map<String, String> params = new HashMap<>();
        params.put("call_time", "1348217629");

        params.put("data", data);
        params.put("sign", DDGroupBuyUtil.sign("send_msg",data, "1348217629"));
        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);

        assertContentType("text/xml", response);
        String desc = (String)renderArgs("desc");
        assertNotNull(desc);
    }

    @Test
    public void 测试发送短信_券已发送短信三次() {
        coupon.expireAt = DateHelper.afterDays(new Date(), 1);
        coupon.smsSentCount = 3;
        coupon.save();

        Map<String, String> params = new HashMap<>();
        params.put("call_time", "1348217629");
        String data = "<data><order>" +
                "<order_id><![CDATA[" + outerOrder.orderId + "]]></order_id>" +
                "<ddgid><![CDATA[" + product.goodsLinkId + "]]></ddgid>" +
                "<spgid><![CDATA[" + product.goodsLinkId + "]]></spgid>" +
                "<user_code><![CDATA[159300520]]></user_code>" +
                "<receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel>" +
                "<consume_id><![CDATA[0159300520]]></consume_id></order></data>";

        String sign = DDGroupBuyUtil.sign("send_msg", data, "1348217629");
        params.put("data", data);
        params.put("sign", sign);

        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);

        assertContentType("text/xml", response);
        String desc = (String)renderArgs("desc");
        assertNotNull(desc);
    }
}
