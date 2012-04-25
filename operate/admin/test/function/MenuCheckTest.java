package function;

import controllers.operate.cas.Security;
import models.admin.OperateRole;
import models.admin.OperateUser;
import operate.rbac.ContextedMenu;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Logger;
import play.mvc.Router;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.List;

public class MenuCheckTest extends FunctionalTest {

    @BeforeClass
    public static void setUpRouter() {
        Router.addRoute("GET", "/foo/bar", "Foo.bar");
        Router.addRoute("GET", "/singlefoo/bar", "SingleFoo.bar");
        Router.addRoute("GET", "/singlefoo/user", "SingleFoo.user");
    }

    @Before
    public void setUp() {
        Fixtures.delete(OperateUser.class);
        Fixtures.delete(OperateRole.class);
        Fixtures.loadModels("fixture/roles.yml");
        Fixtures.loadModels("fixture/supplier_users.yml");

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
        Long id = (Long) Fixtures.idCache.get("models.admin.OperateUser-user3");
        OperateUser user = OperateUser.findById(id);

        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
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
