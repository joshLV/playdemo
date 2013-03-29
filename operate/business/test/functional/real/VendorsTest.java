package functional.real;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import models.order.Vendor;
import models.supplier.Supplier;
import org.junit.After;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 供货商管理测试
 * <p/>
 * User: wangjia
 * Date: 13-3-29
 * Time: 上午9:52
 */
public class VendorsTest extends FunctionalTest {
    Vendor vendor;

    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();
        vendor = FactoryBoy.create(Vendor.class);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @After
    public void tearDown() {
        Supplier.clearShihuiSupplier();
    }


    @Test
    public void testAdd() {
        Http.Response response = GET(Router.reverse("real.Vendors.add").url);
        assertIsOk(response);
    }

    @Test
    public void testCreate() {
        assertEquals(1, Vendor.findAll().size());
        Map<String, String> params = new HashMap<>();
        params.put("vendor.name", "vendor6");
        params.put("vendor.address", "address6");
        params.put("vendor.authorizedRepresentative", "authorizedRepresentative6");
        params.put("vendor.phone", "021-64876543");
        params.put("vendor.fax", "021-64876543");
        params.put("vendor.bankName", "bandName6");
        params.put("vendor.cardNumber", "12345654321");

        Http.Response response = POST(Router.reverse("real.Vendors.create").url, params);
        assertStatus(302, response);
        assertEquals(2, Vendor.findAll().size());
    }

    @Test
    public void testEdit() {
        Http.Response response = GET("/vendors/" + vendor.id + "/edit");

        assertIsOk(response);
        Vendor vendor = (Vendor) renderArgs("vendor");
        assertEquals("authorizedRepresentative", vendor.authorizedRepresentative);
    }

    @Test
    public void testUpdate() {
        String params = "vendor.name=test-update-name&vendor.address=test-update-address";
        Map<String, Object> urlMap = new HashMap<>();
        urlMap.put("id", vendor.id);
        Http.Response response = PUT(Router.reverse("real.Vendors.update", urlMap).url, "application/x-www-form-urlencoded", params);
        assertStatus(302, response);
        vendor.refresh();
        assertEquals("test-update-name", vendor.name);
        assertEquals("test-update-address", vendor.address);

    }


}
