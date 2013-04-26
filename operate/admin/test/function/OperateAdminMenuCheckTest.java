package function;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
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

        operateUser = FactoryBoy.create(OperateUser.class, "with_role");

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
