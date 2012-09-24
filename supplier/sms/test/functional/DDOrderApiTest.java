package functional;

import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.AccountCreditable;
import models.accounts.AccountType;
import models.consumer.User;
import models.dangdang.ErrorCode;
import models.dangdang.ErrorInfo;
import models.order.ECoupon;
import models.order.Order;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.GoodsLevelPrice;
import models.sales.MaterialType;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Before;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-17
 * Time: 下午2:23
 */
public class DDOrderApiTest extends FunctionalTest {
    @Before
    public void setup() {
        FactoryBoy.lazyDelete();

    }


    @Test
    public void 测试创建订单参数有问题的情况() {
//  '3000003'.'push_team_stock'.'1.0'.''.'2012-09-18 16:56:22'
//        String SPID = "3000003";
//        String apiName = "send_msg";
//        String VER = "1.0";
//        String data="<data><order><order_id><![CDATA[4668536249]]></order_id><ddgid><![CDATA[1800003230]]></ddgid><spgid><![CDATA[84173]]></spgid><user_code><![CDATA[91533219]]></user_code><receiver_mobile_tel><![CDATA[13111111111]]></receiver_mobile_tel><consume_id><![CDATA[572467747723]]></consume_id></order></data>";
//        String SECRET_KEY = "x8765d9yj72wevshn";
        String time = "1348123759";
////
//        String s = "all_amount=&amount=&commission_used=0&ctime=1348205004&deal_type_name=code_mine&express_fee=0&id=272&kx_order_id=123456212&options=272%3A3&pay_order_id=123&tcash=0&user_id=8912E83C6C949BB5B96135854807F421&user_mobile=13587469824&sn=x8765d9yj72wevshn";
//        System.out.println("+++++>"+ DigestUtils.md5Hex((SPID + apiName + VER + data + SECRET_KEY + time)));
//        System.out.println("+++++>" + DigestUtils.md5Hex(s + time));
        Goods goods = FactoryBoy.create(Goods.class);
        User user = FactoryBoy.create(User.class);
        Map<String, String> params = new HashMap<>();
        params.put("id", "abcde");
        params.put("deal_type_name", "code_mine");
        Http.Response response = POST("/api/v1/dangdang/order", params);
        assertStatus(200, response);
        ErrorInfo error = (ErrorInfo) renderArgs("errorInfo");
        assertEquals("用户或手机不存在！", error.errorDes);
        assertEquals(ErrorCode.USER_NOT_EXITED, error.errorCode);


        params.put("user_id", user.id.toString());
        params.put("user_mobile", "code_mine");
        response = POST("/api/v1/dangdang/order", params);
        assertStatus(200, response);
        error = (ErrorInfo) renderArgs("errorInfo");
        assertEquals("订单不存在！", error.errorDes);
        assertEquals(ErrorCode.ORDER_NOT_EXITED, error.errorCode);


        params.put("kx_order_id", "12345678");
        params.put("options", goods.id + ":" + "1");
        response = POST("/api/v1/dangdang/order", params);
        assertStatus(200, response);
        error = (ErrorInfo) renderArgs("errorInfo");
        assertEquals("sign验证失败！", error.errorDes);
        assertEquals(ErrorCode.VERIFY_FAILED, error.errorCode);

        params.put("sign", "beefdebebef85f55ecba47d54d8308e8");
        params.put("ctime", String.valueOf(System.currentTimeMillis() / 1000));
        response = POST("/api/v1/dangdang/order", params);
        assertStatus(200, response);
        error = (ErrorInfo) renderArgs("errorInfo");

        assertEquals("sign验证失败！", error.errorDes);
        assertEquals(ErrorCode.VERIFY_FAILED, error.errorCode);
    }


    @Ignore
    @Test
    public void 测试创建订单() {
        Goods goods = FactoryBoy.create(Goods.class);
        goods.materialType = MaterialType.ELECTRONIC;
        goods.save();
        Resaler resaler = FactoryBoy.create(Resaler.class);
        GoodsLevelPrice goodsLevelPrice = FactoryBoy.create(GoodsLevelPrice.class);
        goodsLevelPrice.goods = goods;
        goodsLevelPrice.save();
        Account account = FactoryBoy.create(Account.class);
        account.uid = resaler.id;
        account.accountType = AccountType.RESALER;
        account.creditable = AccountCreditable.YES;
        account.amount = BigDecimal.ONE;
        account.save();

        SortedMap<String, String> params = new TreeMap<>();
        params.put("tcash", "0");
        params.put("express_fee", "0");
        params.put("commission_used", "0");
        params.put("kx_order_id", "12345678");
        params.put("format", "xml");
        params.put("all_amount", "10.0");
        params.put("deal_type_name", "code_mine");
        params.put("ctime", "1284863557");
        params.put("id", "abcde");
        params.put("amount", "10.0");
        params.put("user_mobile", "13764081569");
        params.put("user_id", resaler.id.toString());
        params.put("options", goods.id + ":" + "1");
        String sign = getSign(params);

        params.put("sign", sign);
        Http.Response response = POST("/api/v1/dangdang/order", params);
        assertStatus(200, response);
        Order order = (Order) renderArgs("order");
        String id = (String) renderArgs("ddgid");
        String kx_order_id = (String) renderArgs("kx_order_id");
        assertEquals("abcde", id);
        assertEquals("12345678", kx_order_id);

        assertNotNull(order);
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderNumber",
                OuterOrderPartner.DD, kx_order_id).first();
        System.out.println(outerOrder + "outerOrder");
        assertEquals(order.orderNumber, outerOrder.ybqOrder.orderNumber);
         List<ECoupon> eCouponList = ECoupon.findByOrder(order);
        assertEquals(1, eCouponList.size());

    }

    private String getSign(SortedMap<String, String> params) {
        StringBuilder signStr = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if ("body".equals(entry.getKey()) || "format".equals(entry.getKey())) {
                continue;
            }
            signStr.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue())).append("&");
        }
        signStr.append("sn=").append("x8765d9yj72wevshn");
        return DigestUtils.md5Hex(signStr.toString());
    }
}
