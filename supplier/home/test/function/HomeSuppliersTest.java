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
 * User: wangjia
 * Date: 12-11-27
 * Time: 下午5:51
 */
public class HomeSuppliersTest extends FunctionalTest {
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
    public void testIndex() {
        Http.Response response = GET("/info");
        assertIsOk(response);
        assertEquals(supplier, (Supplier) renderArgs("supplier"));
    }
}
