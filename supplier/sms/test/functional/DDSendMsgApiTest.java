package functional;

import factory.FactoryBoy;
import models.accounts.AccountType;
import models.dangdang.DDAPIUtil;
import models.dangdang.ErrorCode;
import models.dangdang.Response;
import models.order.ECoupon;
import models.order.Order;
import models.order.OuterOrder;
import models.resale.Resaler;
import models.resale.ResalerStatus;
import models.sales.Goods;
import org.junit.Test;
import play.mvc.Before;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-17
 * Time: 下午2:23
 */
public class DDSendMsgApiTest extends FunctionalTest {
    @Before
    public void setup() {
        FactoryBoy.lazyDelete();

    }

    @Test
    public void 测试发送短信() {
        Resaler resaler = FactoryBoy.create(Resaler.class);
        Order order = FactoryBoy.create(Order.class);
        order.userId = resaler.id;
        order.userType = AccountType.RESALER;
        order.save();
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


        params.put("data", data.replace("<order>", "order1"));
        params.put("sign", "f7032ef81047b3a2fcee60b6656f04ae");
        response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        res = (Response) renderArgs("response");
        assertEquals("xml解析失败！", res.desc);
        assertEquals(ErrorCode.PARSE_XML_FAILED, res.errorCode);

        OuterOrder outerOrder = FactoryBoy.create(OuterOrder.class);
        Goods g = FactoryBoy.create(Goods.class);
        data = "<data><order><order_id><![CDATA[12345678]]></order_id><ddgid><![CDATA[" + g.id + "]]></ddgid><spgid><![CDATA[" + g.id + "]]></spgid><user_code><![CDATA[159300520]]></user_code><receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel><consume_id><![CDATA[0159300520]]></consume_id></order></data>";
        String sign = DDAPIUtil.getSign(data, "1348217629", "send_msg");
        params.put("data", data);
        params.put("sign", sign);
        response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        res = (Response) renderArgs("response");
        assertEquals("没找到对应的当当订单!", res.desc);
        assertEquals(ErrorCode.ORDER_NOT_EXITED, res.errorCode);


        outerOrder.ybqOrder = order;
        outerOrder.save();
        resaler.status = ResalerStatus.FREEZE;
        resaler.save();
        params.put("data", data);
        params.put("sign", sign);
        response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        res = (Response) renderArgs("response");
        assertEquals("当当用户不存在！", res.desc);
        assertEquals(ErrorCode.USER_NOT_EXITED, res.errorCode);

        resaler.status = ResalerStatus.APPROVED;
        resaler.save();
        order.userId = 9999l;
        order.save();
        params.put("data", data);
        params.put("sign", sign);
        response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        res = (Response) renderArgs("response");
        assertEquals("没找到对应的订单", res.desc);
        assertEquals(ErrorCode.ORDER_NOT_EXITED, res.errorCode);


        order.orderNumber = "987654321";
        order.userId = resaler.id;
        order.userType = AccountType.RESALER;
        order.save();
        params.put("data", data);
        params.put("sign", sign);
        response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        res = (Response) renderArgs("response");
        assertEquals("没找到对应的券号", res.desc);
        assertEquals(ErrorCode.COUPON_SN_NOT_EXISTED, res.errorCode);


        ECoupon coupon = FactoryBoy.create(ECoupon.class);
        coupon.goods = g;
        coupon.order = order;
        coupon.eCouponSn = "0159300520";
        coupon.save();

        response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        res = (Response) renderArgs("response");
        assertEquals("success", res.desc);
        assertEquals(ErrorCode.SUCCESS, res.errorCode);
        assertEquals("0159300520", res.getAttribute("consumeId"));
        assertEquals("12345678", res.getAttribute("ddOrderId"));
        assertEquals(order.id, res.getAttribute("ybqOrderId"));

        response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);

        response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);

        response = POST("/api/v1/dangdang/send-msg", params);
        assertStatus(200, response);
        res = (Response) renderArgs("response");
        assertEquals("短信发送失败(消费者只有三次发送短信的机会！)", res.desc);
        assertEquals(ErrorCode.MESSAGE_SEND_FAILED, res.errorCode);

    }

}
