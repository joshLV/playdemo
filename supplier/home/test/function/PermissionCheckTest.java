package function;

import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.admin.SupplierPermission;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import navigation.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.mvc.Http.Response;
import play.mvc.Router;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

public class PermissionCheckTest extends FunctionalTest {
    SupplierUser supplierUserAdminSales;
    SupplierUser supplierUserTest;

    @BeforeClass
    public static void setUpRouter() {
        Router.addRoute("GET", "/foo/bar", "Foo.bar");
        Router.addRoute("GET", "/singlefoo/bar", "SingleFoo.bar");
        Router.addRoute("GET", "/singlefoo/user", "SingleFoo.user");
        Router.addRoute("GET", "/singlefoo/google", "SingleFoo.google");
    }

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
            @Override
            public void build(SupplierRole role) {
                role.text = "销售人员";
                role.key = "sales";
            }
        });


        FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
            @Override
            public void build(SupplierRole role) {
                role.text = "系统管理员";
                role.key = "admin";
            }
        });

        FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
            @Override
            public void build(SupplierRole role) {
                role.text = "测试角色";
                role.key = "test";
            }
        });

//        FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
//            @Override
//            public void build(SupplierRole role) {
//                role.text = "商品编辑";
//                role.key = "editor";
//            }
//        });

        FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
            @Override
            public void build(SupplierRole role) {
                role.text = "店员";
                role.key = "clerk";
            }
        });

        FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
            @Override
            public void build(SupplierRole role) {
                role.text = "经理";
                role.key = "manager";
            }
        });

        FactoryBoy.create(SupplierRole.class, new BuildCallback<SupplierRole>() {
            @Override
            public void build(SupplierRole role) {
                role.text = "财务";
                role.key = "account";
            }
        });

        supplierUserAdminSales = FactoryBoy.create(SupplierUser.class, "SalesAdmin");
        supplierUserTest = FactoryBoy.create(SupplierUser.class, "Test");

        // 加载test/rbac.xml配置文件
        VirtualFile file = VirtualFile.open("test/rbac.xml");
        RbacLoader.init(file);
    }

    @After
    public void tearDown() {
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testAdminUserHasNotPermission() {
        printUserRoles(supplierUserAdminSales);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUserAdminSales.loginName);

        Response resp1 = GET("/foo/bar");
        assertStatus(200, resp1);
        assertContentMatch("没有权限", resp1);
        Response resp2 = GET("/singlefoo/bar");
        assertStatus(200, resp2);
        assertContentMatch("没有权限", resp2);
        Response resp3 = GET("/singlefoo/google");
        assertStatus(200, resp3);
        assertContentMatch("没有权限", resp3);
        assertIsOk(GET("/singlefoo/user"));
    }


    @Test
    public void testTestUserHasPermission() {
        printUserRoles(supplierUserTest);

        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUserTest.loginName);
        assertIsOk(GET("/foo/bar"));
        assertIsOk(GET("/singlefoo/bar"));
        assertIsOk(GET("/singlefoo/google"));
        Response resp1 = GET("/singlefoo/user");
        assertStatus(200, resp1);
        assertContentMatch("没有权限", resp1);
    }


    private void printUserRoles(SupplierUser user) {
        System.out.println("User:" + user.loginName + " roles=" + user.roles);
        for (SupplierRole role : user.roles) {
            System.out.println("  role:" + role.key);
            for (SupplierPermission perm : role.permissions) {
                System.out.println("       perm:" + perm.key);
            }
        }


    }

}
