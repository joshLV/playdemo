package function;

import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import models.admin.SupplierUser;
import models.supplier.Supplier;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

/**
 * 商户系统首页的功能测试.
 * <p/>
 * User: sujie
 * Date: 1/11/13
 * Time: 8:48 PM
 */
public class SupplierHomeTest extends FunctionalTest {
    SupplierUser supplierUser;
    Supplier supplier;
    @org.junit.Before
    public void setup() {
        FactoryBoy.lazyDelete();

        supplier = FactoryBoy.create(Supplier.class);
        supplierUser = FactoryBoy.create(SupplierUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
    }

    @Test
    public void testIndex_ForECoupon() {

        supplierUser.save();

        Http.Response response = GET("/");
        assertIsOk(response);
    }

    @Test
    public void testIndex_v2() {
        supplierUser.defaultUiVersion = "v2";
        supplierUser.save();

        Http.Response response = GET("/");
        assertStatus(302, response);
        assertEquals("/ui-version/to/v2", response.getHeader("Location"));
    }

}
