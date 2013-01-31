package functional;

import com.uhuila.common.util.DateUtil;
import factory.FactoryBoy;
import models.accounts.AccountType;
import models.dangdang.DDAPIUtil;
import models.dangdang.ErrorCode;
import models.dangdang.Response;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OuterOrder;
import models.resale.Resaler;
import models.resale.ResalerStatus;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
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
    GoodsDeployRelation deployRelation;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        resaler = FactoryBoy.create(Resaler.class);
        order = FactoryBoy.create(Order.class);
        order.userId = resaler.id;
        order.userType = AccountType.RESALER;
        order.save();
        deployRelation = FactoryBoy.create(GoodsDeployRelation.class);
        g = deployRelation.goods;
        coupon = FactoryBoy.create(ECoupon.class);
        coupon.order = order;
        coupon.eCouponSn = "0159300520";
        coupon.expireAt = DateUtil.getYesterday();
        coupon.goods = g;
        coupon.save();

        outerOrder = FactoryBoy.create(OuterOrder.class);
        outerOrder.ybqOrder = order;
        outerOrder.save();

    }

    @Test
    public void 测试发送短信_正常情况() {
        coupon.expireAt = DateHelper.afterDays(new Date(), 1);
        coupon.save();

        Map<String, String> params = new HashMap<>();
        params.put("call_time", "1348217629");
        String data = "<data><order><order_id><![CDATA[12345678]]></order_id><ddgid><![CDATA[" + deployRelation.linkId + "]]></ddgid><spgid><![CDATA[" + deployRelation.linkId + "]]></spgid><user_code><![CDATA[159300520]]></user_code><receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel><consume_id><![CDATA[0159300520]]></consume_id></order></data>";
        String sign = DDAPIUtil.getSign(data, "1348217629", "send_msg");
        params.put("data", data);
        params.put("sign", sign);

        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        Response res = (Response) renderArgs("response");
        assertEquals("success", res.desc);
    }

    @Test
    public void 测试发送短信_sign出错() {
        String data = "<data><order><order_id><![CDATA[12345678]]></order_id><ddgid><![CDATA[1]]></ddgid><spgid><![CDATA[1]]></spgid><user_code><![CDATA[159300520]]></user_code><receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel><consume_id><![CDATA[0159300520]]></consume_id></order></data>";

        Map<String, String> params = new HashMap<>();
        params.put("data", data);
        params.put("call_time", "1348217629");
        params.put("sign", "");
        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        Response res = (Response) renderArgs("response");
        assertEquals("sign验证失败！", res.desc);
        assertEquals(ErrorCode.VERIFY_FAILED, res.errorCode);

        params.put("sign", "beefdebebef85f55ecba47d54d8308e8");
        response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        res = (Response) renderArgs("response");
        assertEquals("sign验证失败！", res.desc);
        assertEquals(ErrorCode.VERIFY_FAILED, res.errorCode);
    }

    @Test
    @Ignore
    public void 测试发送短信_xml解析失败() {
        String data = "<data><order><order_id><![CDATA[12345678]]></order_id><ddgid><![CDATA[1]]></ddgid><spgid><![CDATA[1]]></spgid><user_code><![CDATA[159300520]]></user_code><receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel><consume_id><![CDATA[0159300520]]></consume_id></order></data>";


        Map<String, String> params = new HashMap<>();
        params.put("call_time", "1348217629");

        params.put("data", data.replace("<order>", "order1"));
        params.put("sign", "f7032ef81047b3a2fcee60b6656f04ae");
        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        Response res = (Response) renderArgs("response");
        assertEquals("xml解析失败！", res.desc);
        assertEquals(ErrorCode.PARSE_XML_FAILED, res.errorCode);
    }

    @Test
    public void 测试发送短信_没找到对应的当当订单() {
        Map<String, String> params = new HashMap<>();
        params.put("call_time", "1348217629");
        String data = "<data><order><order_id><![CDATA[1]]></order_id><ddgid><![CDATA[1]]></ddgid><spgid><![CDATA[1]]></spgid><user_code><![CDATA[159300520]]></user_code><receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel><consume_id><![CDATA[0159300520]]></consume_id></order></data>";
        String sign = DDAPIUtil.getSign(data, "1348217629", "send_msg");
        params.put("data", data);
        params.put("sign", sign);


        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        Response res = (Response) renderArgs("response");
        assertEquals("没找到对应的当当订单!", res.desc);
        assertEquals(ErrorCode.ORDER_NOT_EXITED, res.errorCode);
    }

    @Test
    public void 测试发送短信_当当用户不存在() {
        String data = "<data><order><order_id><![CDATA[12345678]]></order_id><ddgid><![CDATA[1]]></ddgid><spgid><![CDATA[1]]></spgid><user_code><![CDATA[159300520]]></user_code><receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel><consume_id><![CDATA[0159300520]]></consume_id></order></data>";


        Map<String, String> params = new HashMap<>();
        params.put("call_time", "1348217629");

        if (resaler == null) {
            resaler = FactoryBoy.create(Resaler.class);
        }
        resaler.status = ResalerStatus.FREEZE;
        resaler.save();
        params.put("data", data);
        String sign = DDAPIUtil.getSign(data, "1348217629", "send_msg");
        params.put("sign", sign);
        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        Response res = (Response) renderArgs("response");
        assertEquals("当当用户不存在！", res.desc);
        assertEquals(ErrorCode.USER_NOT_EXITED, res.errorCode);
    }

    @Test
    public void 测试发送短信_没找到对应的订单() {
        String data = "<data><order><order_id><![CDATA[12345678]]></order_id><ddgid><![CDATA[1]]></ddgid><spgid><![CDATA[1]]></spgid><user_code><![CDATA[159300520]]></user_code><receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel><consume_id><![CDATA[0159300520]]></consume_id></order></data>";

        Map<String, String> params = new HashMap<>();
        params.put("call_time", "1348217629");

        if (resaler == null) {
            resaler = FactoryBoy.create(Resaler.class);
        }
        resaler.status = ResalerStatus.APPROVED;
        resaler.save();
        order.userId = 9999l;
        order.save();

        data = "<data><order><order_id><![CDATA[12345678]]></order_id><ddgid><![CDATA[" + deployRelation.linkId + "]]></ddgid><spgid><![CDATA[" + deployRelation.linkId + "]]></spgid><user_code><![CDATA[159300520]]></user_code><receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel><consume_id><![CDATA[0159300520]]></consume_id></order></data>";
        String sign = DDAPIUtil.getSign(data, "1348217629", "send_msg");
        params.put("data", data);
        params.put("sign", sign);
        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        Response res = (Response) renderArgs("response");
        assertEquals("没找到对应的订单!", res.desc);
        assertEquals(ErrorCode.ORDER_NOT_EXITED, res.errorCode);
    }

    @Test
    public void 测试发送短信_没找到对应的券号() {
        String data = "<data><order><order_id><![CDATA[12345678]]></order_id><ddgid><![CDATA[1]]></ddgid><spgid><![CDATA[1]]></spgid><user_code><![CDATA[159300520]]></user_code><receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel><consume_id><![CDATA[0159300520]]></consume_id></order></data>";
        Map<String, String> params = new HashMap<>();
        params.put("call_time", "1348217629");

        order.orderNumber = "987654321";
        order.save();
        params.put("data", data);
        String sign = DDAPIUtil.getSign(data, "1348217629", "send_msg");
        params.put("sign", sign);
        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        Response res = (Response) renderArgs("response");
        assertEquals("没找到对应的券号!", res.desc);
        assertEquals(ErrorCode.COUPON_SN_NOT_EXISTED, res.errorCode);
    }

    @Test
    public void 测试发送短信_券已过期() {

        String data = "<data><order><order_id><![CDATA[12345678]]></order_id><ddgid><![CDATA[1]]></ddgid><spgid><![CDATA[" + deployRelation.linkId + "]]></spgid><user_code><![CDATA[159300520]]></user_code><receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel><consume_id><![CDATA[0159300520]]></consume_id></order></data>";

        Map<String, String> params = new HashMap<>();
        params.put("call_time", "1348217629");
        params.put("data", data);
        params.put("sign", DDAPIUtil.getSign(data, "1348217629", "send_msg"));
        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        Response res = (Response) renderArgs("response");
        assertEquals("对不起该券已过期，不能重发短信！", res.desc);
        assertEquals(ErrorCode.COUPON_EXPIRED, res.errorCode);
    }

    @Test
    public void 测试发送短信_券已退款() {
        coupon.status = ECouponStatus.REFUND;
        coupon.save();

        String data = "<data><order><order_id><![CDATA[12345678]]></order_id><ddgid><![CDATA[1]]></ddgid><spgid><![CDATA[" + deployRelation.linkId + "]]></spgid><user_code><![CDATA[159300520]]></user_code><receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel><consume_id><![CDATA[0159300520]]></consume_id></order></data>";
        Map<String, String> params = new HashMap<>();
        params.put("call_time", "1348217629");

        params.put("data", data);
        params.put("sign", DDAPIUtil.getSign(data, "1348217629", "send_msg"));
        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        Response res = (Response) renderArgs("response");
        assertEquals("对不起该券已退款，不能重发短信！", res.desc);
        assertEquals(ErrorCode.COUPON_REFUND, res.errorCode);
    }

    @Test
    public void 测试发送短信_券已发送短信三次() {
        coupon.expireAt = DateHelper.afterDays(new Date(), 1);
        coupon.save();

        Map<String, String> params = new HashMap<>();
        params.put("call_time", "1348217629");
        String data = "<data><order><order_id><![CDATA[12345678]]></order_id><ddgid><![CDATA[" + deployRelation.linkId + "]]></ddgid><spgid><![CDATA[" + deployRelation.linkId + "]]></spgid><user_code><![CDATA[159300520]]></user_code><receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel><consume_id><![CDATA[0159300520]]></consume_id></order></data>";
        String sign = DDAPIUtil.getSign(data, "1348217629", "send_msg");
        params.put("data", data);
        params.put("sign", sign);

        Http.Response response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        Response res = (Response) renderArgs("response");
        assertEquals("success", res.desc);
        assertEquals(ErrorCode.SUCCESS, res.errorCode);

        assertEquals("0159300520", res.getAttribute("consumeId"));
        assertEquals("12345678", res.getAttribute("ddOrderId"));
        assertEquals(order.id, res.getAttribute("ybqOrderId"));
    }
}
