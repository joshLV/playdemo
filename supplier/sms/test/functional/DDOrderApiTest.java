package functional;

import factory.FactoryBoy;
import models.consumer.User;
import models.dangdang.ErrorCode;
import models.dangdang.ErrorInfo;
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
        Http.Response response = POST("/ddApi/order/create", params);
        assertStatus(200, response);
        ErrorInfo error = (ErrorInfo) renderArgs("errorInfo");
        assertEquals("用户不存在！", error.errorDes);
        assertEquals(ErrorCode.USER_NOT_EXITED, error.errorCode);


        params.put("user_id", user.id.toString());
        params.put("user_mobile", "code_mine");
        response = POST("/ddApi/order/create", params);
        assertStatus(200, response);
        error = (ErrorInfo) renderArgs("errorInfo");
        assertEquals("订单不存在！", error.errorDes);
        assertEquals(ErrorCode.ORDER_NOT_EXITED, error.errorCode);


        params.put("kx_order_id", "12345678");
        params.put("options", goods.id + ":" + "1");
        response = POST("/ddApi/order/create", params);
        assertStatus(200, response);
        error = (ErrorInfo) renderArgs("errorInfo");
        assertEquals("sign不存在！", error.errorDes);
        assertEquals(ErrorCode.VERIFY_FAILED, error.errorCode);


        params.put("sign", "f3f4688c1cfe1cc709ffd29cde340413");
        params.put("ctime", String.valueOf(System.currentTimeMillis() / 1000));
        response = POST("/ddApi/order/create", params);
        assertStatus(200, response);
        error = (ErrorInfo) renderArgs("errorInfo");

        assertEquals("sign验证失败！", error.errorDes);
        assertEquals(ErrorCode.VERIFY_FAILED, error.errorCode);

    }
}
