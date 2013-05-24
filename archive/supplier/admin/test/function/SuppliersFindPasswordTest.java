package function;

import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import models.admin.SupplierUser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.cache.Cache;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试找回密码
 * <p/>
 * User: wangjia
 * Date: 12-12-11
 * Time: 上午10:02
 */
public class SuppliersFindPasswordTest extends FunctionalTest {
    SupplierUser supplierUser;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        supplierUser = FactoryBoy.create(SupplierUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
    }

    @After
    public void tearDown() {
        Cache.clear();
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/forget-password");
        assertIsOk(response);
        assertContentMatch("验证手机", response);
    }

    @Test
    public void testCheckByTel() {
        Map<String, String> params = new HashMap<>();
        params.put("mobile", supplierUser.mobile);
        Http.Response response = POST("/checkByTel", params);
        assertStatus(200, response);
        assertEquals("1", response.out.toString());
    }

    @Test
    public void testReset() {
        Map<String, String> params = new HashMap<>();
        Cache.set("validCode_", "123456", "30mn");
        Cache.set("mobile_", supplierUser.mobile, "30mn");
        params.put("mobile", supplierUser.mobile);
        params.put("validCode", "123456");
        Http.Response response = POST("/reset", params);
        assertStatus(200, response);
        assertEquals("0", response.out.toString());
    }

    @Test
    public void testResetPassword() {
        Cache.set("mobile_", supplierUser.mobile, "30mn");
        Http.Response response = GET("/reset-password");
        assertIsOk(response);
        assertEquals(supplierUser.mobile, renderArgs("mobile"));
    }

    @Test
    public void testUpdatePassword() {
        Cache.set("mobile_", supplierUser.mobile);
        Map<String, String> params = new HashMap<>();
        params.put("supplierUserId", supplierUser.id.toString());
        params.put("mobile", supplierUser.mobile);
        params.put("password", "123456");
        params.put("confirmPassword", "123456");
        Http.Response response = POST("/reset-password", params);
        assertStatus(200, response);
        assertEquals("1", response.out.toString());
    }


}
