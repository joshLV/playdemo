package function;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.admin.OperateUser;
import models.supplier.SupplierCategory;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.Map;

/**
 * User: wangjia
 * Date: 12-12-3
 * Time: 上午11:26
 */
public class SuppliersCategoryTest extends FunctionalTest {
    SupplierCategory supplierCategory;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        supplierCategory = FactoryBoy.create(SupplierCategory.class);
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/suppliers_category");
        assertStatus(200, response);
        assertEquals(1, ((JPAExtPaginator<SupplierCategory>) renderArgs("supplierCategoryPage")).size());

    }

    @Test
    public void testAdd() {
        Http.Response response = GET("/suppliers_category/new");
        assertStatus(200, response);
        assertContentMatch("添加商户类别", response);
    }

    @Test
    public void testEdit() {
        Http.Response response = GET("/suppliers_category/" + supplierCategory.id + "/edit");
        assertStatus(200, response);
        assertEquals(supplierCategory, (SupplierCategory) renderArgs("supplierCategory"));
    }

    @Test
    public void testCreate() {
        assertEquals(1, SupplierCategory.count());
        Map<String, String> itemParams = new HashMap<>();
        itemParams.put("supplierCategory.code", "02");
        itemParams.put("supplierCategory.name", "create_name");
        Http.Response response = POST("/suppliers_category", itemParams);
        assertStatus(302, response);
        assertEquals(2, SupplierCategory.count());
    }

    @Test
    public void testUpdate() {
        String params = "id=" + supplierCategory.id + "&supplierCategory.name=update_name";
        Http.Response response = PUT("/suppliers_category/" + supplierCategory.id, "application/x-www-form-urlencoded", params);
        assertStatus(302, response);
        supplierCategory.refresh();
        assertEquals(supplierCategory.name, "update_name");
    }


}
