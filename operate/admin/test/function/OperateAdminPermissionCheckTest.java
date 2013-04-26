package function;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.operator.OperatePermission;
import models.operator.OperateRole;
import models.operator.OperateUser;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.mvc.Http.Response;
import play.mvc.Router;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

public class OperateAdminPermissionCheckTest extends FunctionalTest {
    OperateUser operateUser;
    String[] roleText = {"销售人员",  "编辑", "客服", "财务"};
    String[] roleKey = {"sales", "editor", "customservice", "account"};
    int index = 0;

    @BeforeClass
    public static void setUpRouter() {
        Router.addRoute("GET", "/foo/bar", "data.Foo.bar");
        Router.addRoute("GET", "/singlefoo/bar", "data.SingleFoo.bar");
        Router.addRoute("GET", "/singlefoo/user", "data.SingleFoo.user");
        Router.addRoute("GET", "/singlefoo/google", "data.SingleFoo.google");
    }

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        FactoryBoy.batchCreate(4, OperateRole.class,
                new SequenceCallback<OperateRole>() {
                    @Override
                    public void sequence(OperateRole role, int seq) {
                        role.text = roleText[index];
                        role.key = roleKey[index++];
                    }
                });


        operateUser = FactoryBoy.create(OperateUser.class);

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
//        printUserRoles(operateUser);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);

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
//        printUserRoles(operateUser);

        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);

        assertIsOk(GET("/foo/bar"));
        assertIsOk(GET("/singlefoo/bar"));
        assertIsOk(GET("/singlefoo/google"));
        Response resp1 = GET("/singlefoo/user");
        assertStatus(200, resp1);
        assertContentMatch("没有权限", resp1);
    }


    private void printUserRoles(OperateUser user) {
        for (OperateRole role : user.roles) {
            System.out.println("  role:" + role.key);
            for (OperatePermission perm : role.permissions) {
                System.out.println("       perm:" + perm.key);
            }
        }
    }

}
