package function;

import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import models.admin.SupplierUser;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

/**
 * User: tanglq
 * Date: 12-12-27
 * Time: 下午5:10
 */
public class SupplierChangeUIVersionsTest extends FunctionalTest {

    Supplier supplier;
    SupplierUser supplierUser;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        supplier = FactoryBoy.create(Supplier.class);
        supplierUser = FactoryBoy.create(SupplierUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
    }

    @Test
    public void testChangeToV1() {
        Http.Response response = GET("/ui-version/to/v1");
        assertStatus(302, response);
        assertEquals("http://localhost.order.uhuila.net/coupons/single", response.getHeader("Location"));

        supplierUser.refresh();
        assertEquals("v1", supplierUser.defaultUiVersion);
    }


    @Test
    public void testChangeToV2() {
        Http.Response response = GET("/ui-version/to/v2");
        assertStatus(302, response);
        assertEquals("http://localhost.home.uhuila.net/", response.getHeader("Location"));

        supplierUser.refresh();
        assertEquals("v2", supplierUser.defaultUiVersion);
    }
}
