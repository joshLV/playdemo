package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import models.consumer.User;
import models.consumer.UserStatus;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.List;

/**
 * User: wangjia
 * Date: 12-11-22
 * Time: 上午9:29
 */
public class OperateConsumersTest extends FunctionalTest {
    User user;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser operateUser = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);
        user = FactoryBoy.create(User.class);
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/consumers");
        assertStatus(200, response);
        assertEquals(1, ((List<User>) renderArgs("users")).size());
        assertEquals(user.loginName, ((List<User>) renderArgs("users")).get(0).loginName);
    }

    @Test
    public void testShow() {
        Http.Response response = GET("/consumers/" + user.id);
        assertStatus(200, response);
        assertEquals(user.loginName, ((User) renderArgs("user")).loginName);
    }

    @Test
    public void testFreeze() {
        assertEquals(user.status, UserStatus.NORMAL);
        Http.Response response = PUT("/consumers/" + user.id + "/freeze", "text/html", "");
        user.refresh();
        assertEquals(user.status, UserStatus.FREEZE);
        assertEquals(user.status, UserStatus.FREEZE);
    }

    @Test
    public void testUnfreeze() {
        user.status = UserStatus.FREEZE;
        user.save();
        assertEquals(user.status, UserStatus.FREEZE);
        Http.Response response = PUT("/consumers/" + user.id + "/unfreeze", "text/html", "");
        user.refresh();
        assertEquals(user.status, UserStatus.NORMAL);
    }




}
