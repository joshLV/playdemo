package function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.sales.Shop;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import play.vfs.VirtualFile;

public class SupplierUsersTest extends FunctionalTest {
    SupplierUser supplierUser;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();


        supplierUser = FactoryBoy.create(SupplierUser.class);

        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
    }

    @Test
    public void testAdd() {
        Response response = GET("/users/new");
        assertStatus(200, response);
        assertEquals(1, ((List<Shop>) renderArgs("shopList")).size());
    }

    /**
     * 查看操作员信息
     */
    @Test
    public void testIndexNoRight() {
        Response response = GET("/users");
        assertStatus(302, response);
    }


    @Test
    public void testCreate() {
        Long cnt = SupplierUser.count();
        assertEquals(1, cnt.intValue());
        Map<String, String> params = new HashMap<>();
        params.put("supplierUser.loginName", "test");
        params.put("supplierUser.mobile", "1300004001");
        params.put("supplierUser.jobNumber", "134001");
        params.put("supplierUser.encryptedPassword", "123456");
        params.put("supplierUser.confirmPassword", "123456");

        Response response = POST("/users", params);
        assertStatus(302, response);
        assertEquals(cnt + 1, SupplierUser.count());
    }

    @Test
    public void testEdit() {
        Response response = GET("/users/" + supplierUser.id + "/edit");
        assertStatus(200, response);
        SupplierUser supplierUser1 = (SupplierUser) renderArgs("supplierUser");
        String roleIds = (String) renderArgs("roleIds");
        assertNotNull(roleIds);
        List<Shop> shopList = (List<Shop>) renderArgs("shopList");
        assertEquals(supplierUser.loginName, supplierUser1.loginName);
        assertEquals(supplierUser.shop, shopList.get(0));
    }

    @Test
    public void testUpdate() {
        Map<String, String> params = new HashMap<>();
        params.put("supplierUser.encryptedPassword", "895623");
        params.put("supplierUser.confirmPassword", "895623");
        params.put("supplierUser.id", supplierUser.id.toString());
        params.put("supplierUser.supplierId", supplierUser.supplier.id.toString());
        params.put("supplierUser.loginName", supplierUser.loginName);
        params.put("supplierUser.mobile", supplierUser.mobile);
        params.put("supplierUser.jobNumber", "9988");
        Response response = POST("/users/" + supplierUser.id, params);
        assertStatus(302, response);
        supplierUser.refresh();
        assertEquals(supplierUser.jobNumber, "9988");
    }

    //测试是否存在用户名和手机
    @Test
    public void testCheckValue() {
        Map<String, String> params = new HashMap<>();
        params.put("loginName", "test");
        params.put("mobile", "1300000001");
        params.put("id", supplierUser.id.toString());
        Response response = POST("/users/checkLoginName", params);
        assertStatus(200, response);
        assertEquals("0", response.out.toString());
    }

    @Test
    public void testDelete() {
        Response response = DELETE("/users/" + supplierUser.id);
        assertStatus(302, response);
        supplierUser.refresh();
        assertEquals(com.uhuila.common.constants.DeletedStatus.DELETED, supplierUser.deleted);
    }
}
