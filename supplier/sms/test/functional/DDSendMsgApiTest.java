package functional;

import factory.FactoryBoy;
import models.accounts.AccountType;
import models.dangdang.DDOrder;
import models.order.ECoupon;
import models.order.Order;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.MaterialType;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import play.mvc.Before;
import play.test.FunctionalTest;

import java.util.Map;
import java.util.SortedMap;

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
        Goods goods = FactoryBoy.create(Goods.class);
        goods.materialType = MaterialType.ELECTRONIC;
        goods.save();
        Resaler resaler = FactoryBoy.create(Resaler.class);
        Order order = FactoryBoy.create(Order.class);
        order.userId = resaler.id;
        order.userType = AccountType.RESALER;
        order.save();
        DDOrder ddOrder = FactoryBoy.create(DDOrder.class);
        ECoupon coupon = FactoryBoy.create(ECoupon.class);

//        Map<String, String> params = new HashMap<>();
//        params.put("tcash", "0");
//        params.put("express_fee", "0");
//        params.put("commission_used", "0");
//        params.put("kx_order_id", "12345678");
//        params.put("format", "xml");
//        params.put("all_amount", "10.0");
//        params.put("deal_type_name", "code_mine");
//        params.put("ctime", "1284863557");
//        params.put("id", "abcde");
//        params.put("amount", "10.0");
//        params.put("user_mobile", "13764081569");
//        params.put("user_id", resaler.id.toString());
//        params.put("options", goods.id + ":" + "1");
//        String sign = getSign(params);
//        params.put("sign", sign);
//        Http.Response response = POST("/ddApi/order", params);
//        assertStatus(200, response);
//        Order order = (Order) renderArgs("order");
//        String id = (String) renderArgs("id");
//        String kx_order_id = (String) renderArgs("kx_order_id");
//        assertEquals("abcde", id);
//        assertEquals("12345678", kx_order_id);


    }

    private String getSign(Map<String, String> params) {
        StringBuilder signStr = new StringBuilder();
        for (SortedMap.Entry<String, String> entry : params.entrySet()) {
            if ("body".equals(entry.getKey()) || "sign".equals(entry.getKey())) {
                continue;
            }
            signStr.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        signStr.append("secret_key=").append("x8765d9yj72wevshn");
        return DigestUtils.md5Hex(signStr.toString());
    }
}
