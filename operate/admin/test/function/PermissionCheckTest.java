package function;

import models.admin.OperatePermission;
import models.admin.OperateRole;
import models.admin.OperateUser;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.mvc.Http.Response;
import play.mvc.Router;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import controllers.operate.cas.Security;

public class PermissionCheckTest extends FunctionalTest {

    @BeforeClass
    public static void setUpRouter() {
        Router.addRoute("GET", "/foo/bar", "Foo.bar");
        Router.addRoute("GET", "/singlefoo/bar", "SingleFoo.bar");
        Router.addRoute("GET", "/singlefoo/user", "SingleFoo.user");
        Router.addRoute("GET", "/singlefoo/google", "SingleFoo.google");
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
    public void testAdminUserHasNotPermission() {
        Long id = (Long) Fixtures.idCache.get("models.admin.OperateUser-user3");
        OperateUser user = OperateUser.findById(id);
        printUserRoles(user);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

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
        Long id = (Long) Fixtures.idCache.get("models.admin.OperateUser-test");
        OperateUser user = OperateUser.findById(id);
        printUserRoles(user);

        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        assertIsOk(GET("/foo/bar"));
        assertIsOk(GET("/singlefoo/bar"));
        assertIsOk(GET("/singlefoo/google"));
        Response resp1 = GET("/singlefoo/user");
        assertStatus(200, resp1);
        assertContentMatch("没有权限", resp1);
    }


    private void printUserRoles(OperateUser user) {
        System.out.println("User:" + user.loginName + " roles=" + user.roles);
        for (OperateRole role : user.roles) {
            System.out.println("  role:" + role.key);
            for (OperatePermission perm  : role.permissions) {
                System.out.println("       perm:" + perm.key);
            }
        }
    }

}
