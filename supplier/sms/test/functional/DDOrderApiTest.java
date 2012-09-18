package functional;

import factory.FactoryBoy;
import models.consumer.User;
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
public class DDOrderApiTest extends FunctionalTest {
    @Before
    public void setup() {
        FactoryBoy.lazyDelete();

    }

    @Test
    public void 测试创建订单() {
        Goods goods = FactoryBoy.create(Goods.class);
        User user = FactoryBoy.create(User.class);
        Map<String, String> params = new HashMap<>();
        params.put("id", "abcde");
        params.put("deal_type_name", "code_mine");
        params.put("kx_order_id", "12345678");
        params.put("user_id", user.id.toString());
        params.put("user_mobile", "code_mine");
        params.put("options", goods.id + ":" + "1");
        params.put("sign", "f3f4688c1cfe1cc709ffd29cde340413");
        params.put("ctime", String.valueOf(System.currentTimeMillis() / 1000));

        Http.Response response = POST("/ddApi/order/create", params);
        assertStatus(302, response);


    }
}
