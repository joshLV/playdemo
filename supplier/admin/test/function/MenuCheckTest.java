package function;

import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import navigation.ContextedMenu;
import navigation.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.mvc.Router;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.List;

public class MenuCheckTest extends FunctionalTest {
    SupplierUser supplierUserAdminSales;

    @BeforeClass
    public static void setUpRouter() {
        Router.addRoute("GET", "/foo/bar", "Foo.bar");
        Router.addRoute("GET", "/singlefoo/bar", "SingleFoo.bar");
        Router.addRoute("GET", "/singlefoo/user", "SingleFoo.user");
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
//
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
    public void testMenuBaseUrl() {
        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUserAdminSales.loginName);
        assertIsOk(GET("/singlefoo/user"));


        List<ContextedMenu> list = (List<ContextedMenu>) renderArgs("topMenus");

        assertTrue(list.size() > 0);
        assertTrue(list.get(0).getBaseUrl().indexOf("localhost") > 0);
    }
}
