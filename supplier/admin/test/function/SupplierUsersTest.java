package function;

import factory.FactoryBoy;
import models.admin.SupplierUser;
import org.junit.Test;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

public class SupplierUsersTest extends FunctionalTest {
    SupplierUser supplierUser;

    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();
        supplierUser = FactoryBoy.create(SupplierUser.class);
    }


    /**
     * 查看操作员信息
     */
    @Test
    public void testIndex() {
        Response response = GET("/users");
        assertStatus(302, response);
    }

    //测试是否存在用户名和手机
    @Test
    public void testCheckValue() {
        Map<String, String> params = new HashMap<>();
        params.put("loginName", "test");
        params.put("mobile", "1300000001");
        params.put("id", supplierUser.id.toString());
        Response response = POST("/users/checkLoginName", params);
        assertStatus(302, response);
    }
}
