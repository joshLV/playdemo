package functional;

import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.AccountCreditable;
import models.accounts.AccountType;
import models.dangdang.ErrorCode;
import models.dangdang.ErrorInfo;
import models.order.ECoupon;
import models.order.Order;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import models.sales.MaterialType;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-17
 * Time: 下午2:23
 */
public class DDOrderApiTest extends FunctionalTest {
    GoodsDeployRelation deployRelation;


    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        deployRelation = FactoryBoy.create(GoodsDeployRelation.class);
    }

    @Test
    public void 测试创建当当订单_用户不存在() {
        Map<String, String> params = new HashMap<>();
        params.put("id", "abcde");
        params.put("deal_type_name", "code_mine");
        Http.Response response = POST("/api/v1/dangdang/order", params);
        assertStatus(200, response);
        ErrorInfo error = (ErrorInfo) renderArgs("errorInfo");
        assertEquals("用户或手机不存在！", error.errorDes);
        assertEquals(ErrorCode.USER_NOT_EXITED, error.errorCode);
    }

    @Test
    public void 测试创建当当订单_订单不存在() {
        Map<String, String> params = new HashMap<>();
        params.put("id", "abcde");
        params.put("deal_type_name", "code_mine");
        params.put("user_id", "asdf");
        params.put("user_mobile", "code_mine");
        Http.Response response = POST("/api/v1/dangdang/order", params);
        assertStatus(200, response);
        ErrorInfo error = (ErrorInfo) renderArgs("errorInfo");
        assertEquals("订单不存在！", error.errorDes);
        assertEquals(ErrorCode.ORDER_NOT_EXITED, error.errorCode);
    }

    @Test
    public void 测试创建当当订单_验证失败() {
        Map<String, String> params = new HashMap<>();
        Goods goods = deployRelation.goods;
        params.put("id", "abcde");
        params.put("deal_type_name", "code_mine");
        params.put("user_id", "asdf");
        params.put("user_mobile", "code_mine");
        params.put("kx_order_id", "12345678");
        params.put("options", goods.id + ":" + "1");
        Http.Response response = POST("/api/v1/dangdang/order", params);
        assertStatus(200, response);
        ErrorInfo error = (ErrorInfo) renderArgs("errorInfo");
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


    @Test
    public void 测试创建订单() {
        Goods goods = deployRelation.goods;
        goods.materialType = MaterialType.ELECTRONIC;
        goods.save();
        Resaler resaler = FactoryBoy.create(Resaler.class);

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
        params.put("all_amount", "5.0");
        params.put("deal_type_name", "code_mine");
        params.put("ctime", "1284863557");
        params.put("id", "abcde");
        params.put("amount", "5.0");
        params.put("user_mobile", "13764081569");
        params.put("user_id", resaler.id.toString());
        params.put("options", deployRelation.linkId + ":" + "1");
        String sign = getSign(params);

        params.put("sign", sign);
        Http.Response response = POST("/api/v1/dangdang/order", params);
        assertStatus(200, response);
        Order order = (Order) renderArgs("order");
        String id = (String) renderArgs("id");
        String kx_order_id = (String) renderArgs("kx_order_id");
        assertEquals("abcde", id);
        assertEquals("12345678", kx_order_id);

        assertNotNull(order);
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId",
                OuterOrderPartner.DD, kx_order_id).first();
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
