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
 * 商户系统首页的功能测试.
 * <p/>
 * User: sujie
 * Date: 1/11/13
 * Time: 8:48 PM
 */
public class SupplierHomeTest extends FunctionalTest {
    SupplierUser supplierUser;
    Supplier supplier;

    @Before
    public void setup() {
        FactoryBoy.lazyDelete();

        supplier = FactoryBoy.create(Supplier.class);
        supplierUser = FactoryBoy.create(SupplierUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
    }

    @Test
    public void testIndexForDefault() {
        supplier.setProperty(Supplier.SELL_ECOUPON, "1");
        supplier.setProperty(Supplier.CAN_SALE_REAL, "1");

        Http.Response response = GET("/");
        assertStatus(302, response);
        assertEquals("/verify", response.getHeader("Location"));
    }

    @Test
    public void testIndexForECoupon() {
        supplier.setProperty(Supplier.SELL_ECOUPON, "1");
        supplier.setProperty(Supplier.CAN_SALE_REAL, "1");

        Http.Response response = GET("/");
        assertStatus(302, response);
        assertEquals("/verify", response.getHeader("Location"));
    }

    @Test
    public void testIndexForReal() {
        supplier.setProperty(Supplier.SELL_ECOUPON, "0");
        supplier.setProperty(Supplier.CAN_SALE_REAL, "1");

        Http.Response response = GET("/");
        assertStatus(302, response);
        assertEquals("/real/download-order-shipping", response.getHeader("Location"));
    }

}
