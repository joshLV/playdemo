package function;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.operator.OperateRole;
import models.operator.OperateUser;
import operate.rbac.ContextedMenu;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Logger;
import play.mvc.Router;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.List;

public class OperateAdminMenuCheckTest extends FunctionalTest {
    OperateUser operateUser;

    @BeforeClass
    public static void setUpRouter() {
        Router.addRoute("GET", "/foo/bar", "data.Foo.bar");
        Router.addRoute("GET", "/singlefoo/bar", "data.SingleFoo.bar");
        Router.addRoute("GET", "/singlefoo/user", "data.SingleFoo.user");
    }

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        OperateRole roleSales = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "销售人员";
                role.key = "sales";
            }
        });


        OperateRole roleAdmin = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "系统管理员";
                role.key = "admin";
            }
        });

        OperateRole roleTest = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "测试角色";
                role.key = "test";
            }
        });

        OperateRole roleEditor = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "编辑";
                role.key = "editor";
            }
        });

        OperateRole roleCustomservice = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "客服";
                role.key = "customservice";
            }
        });

        OperateRole roleWebop = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "网站运营";
                role.key = "webop";
            }
        });

        OperateRole roleManager = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "经理";
                role.key = "manager";
            }
        });

        OperateRole roleAccount = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "财务";
                role.key = "account";
            }
        });
        operateUser = FactoryBoy.create(OperateUser.class, new BuildCallback<OperateUser>() {
            @Override
            public void build(OperateUser ou) {
                ou.mobile = "13211111111";
            }
        });


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
        operateUser.refresh();
        Security.setLoginUserForTest(operateUser.loginName);
        assertIsOk(GET("/singlefoo/user"));


        List<ContextedMenu> list = (List<ContextedMenu>) renderArgs("topMenus");

        Logger.info("        -----------------------------");
        for (ContextedMenu contextedMenu : list) {
            Logger.info("url=" + contextedMenu.getBaseUrl());
        }

        assertTrue(list.size() > 0);
        assertTrue(list.get(0).getBaseUrl().indexOf("http") >= 0);
    }
}
