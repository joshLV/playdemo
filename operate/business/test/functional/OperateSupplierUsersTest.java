package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.admin.SupplierUser;
import models.admin.SupplierUserType;
import models.operator.OperateUser;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: wangjia
 * Date: 12-11-29
 * Time: 上午10:37
 */
public class OperateSupplierUsersTest extends FunctionalTest {
    Supplier supplier;
    SupplierUser supplierUser;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        supplier = FactoryBoy.create(Supplier.class);
        supplierUser = FactoryBoy.create(SupplierUser.class, new BuildCallback<SupplierUser>() {
            @Override
            public void build(SupplierUser supplierUser) {
                supplierUser.supplierUserType = SupplierUserType.HUMAN;
            }
        });
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/users?loginName=" + supplierUser.loginName + "&userName=" + supplierUser.userName +
                "&jobNumber" + supplierUser.jobNumber + "&supplierId=" + supplierUser.supplier.id);
        assertStatus(200, response);
        assertEquals(1, ((JPAExtPaginator<SupplierUser>) renderArgs("supplierUsersPage")).size());
    }

    @Test
    public void testAdd() {
        Http.Response response = GET("/users/new");
        assertStatus(200, response);
        assertEquals(1, ((List<Supplier>) renderArgs("supplierList")).size());
    }


    @Test
    public void testCreate() {
        assertEquals(1, SupplierUser.count());
        Map<String, String> params = new HashMap<>();
        params.put("supplierUser.encryptedPassword", "895623");
        params.put("supplierUser.confirmPassword", "895623");
        params.put("supplierUser.jobNumber", "1234");
        params.put("supplierUser.mobile", supplierUser.mobile);
        params.put("supplierUser.supplier.id", supplier.id.toString());
        params.put("supplierUser.loginName", "test-loginName");
        Http.Response response = POST("/users", params);
        assertStatus(302, response);
        assertEquals(2, SupplierUser.count());
    }


    @Test
    public void testCheckLoginName() {
        Map<String, String> params = new HashMap<>();
        params.put("id", supplierUser.id.toString());
        params.put("supplierId", supplierUser.supplier.id.toString());
        params.put("supplierUser.loginName", supplierUser.loginName);
        params.put("supplierUser.mobile", supplierUser.mobile);
        params.put("supplierUser.jobNumber", supplierUser.jobNumber);
        Http.Response response = POST("/users/checkLoginName", params);
        assertContentType("application/json", response);
    }

    @Test
    public void testEdit() {
        Http.Response response = GET("/users/" + supplierUser.id + "/edit");
        assertStatus(200, response);
        assertEquals(supplierUser, ((SupplierUser) (renderArgs("supplierUser"))));
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
        Http.Response response = POST("/users/" + supplierUser.id, params);
        assertStatus(302, response);
        supplierUser.refresh();
        assertEquals(supplierUser.jobNumber, "9988");
    }

    @Test
    public void testDelete() {
        Http.Response response = DELETE("/users/" + supplierUser.id);
        assertStatus(302, response);
        supplierUser.refresh();
        assertEquals(com.uhuila.common.constants.DeletedStatus.DELETED, supplierUser.deleted);
    }

    /**
     * 访问微信内容页面，会看到一个识别码.
     *
     * @throws Exception
     */
    @Test
    public void testShowWeixi() throws Exception {
        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("id", supplierUser.id);
        Http.Response response = GET(Router.reverse("OperateSupplierUsers.showWeixi", urlParams).url);
        assertIsOk(response);
        supplierUser.refresh();
        assertNotNull(supplierUser.idCode);
    }

    @Test
    public void testUnbindWeixi() throws Exception {
        supplierUser.weixinOpenId = "3232412341234124";
        supplierUser.save();
        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("id", supplierUser.id);
        Http.Response response = GET(Router.reverse("OperateSupplierUsers.unbindWeixi", urlParams).url);
        assertStatus(302, response);
        supplierUser.refresh();
        assertNull(supplierUser.weixinOpenId);
    }
}
