package function;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.admin.OperateUser;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-11-30
 * Time: 上午9:33
 * To change this template use File | Settings | File Templates.
 */
public class OperateUsersProfilesTest extends FunctionalTest {
    OperateUser operateUser;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        operateUser = FactoryBoy.create(OperateUser.class);
        // 加载test/rbac.xml配置文件
        VirtualFile file = VirtualFile.open("test/rbac.xml");
        RbacLoader.init(file);
        Security.setLoginUserForTest(operateUser.loginName);

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
    public void testTest() {
        Http.Response response = GET("/profile");
        assertStatus(200, response);
        assertEquals(operateUser, (OperateUser) renderArgs("operateUser"));
    }


    @Test
    public void testUpdate() {
        Map<String, String> params = new HashMap<>();
        params.put("operateUser.loginName", "test-loginName");
        params.put("operateUser.userName", "test-userName");
        params.put("operateUser.mobile", "13901895623");
        params.put("operateUser.email", operateUser.email);
        params.put("operateUser.jobNumber", operateUser.jobNumber);
        params.put("operateUser.roles", operateUser.roles.toString());
        params.put("operateUser.encryptedPassword", "123456");
        Http.Response response = POST("/profile", params);
        assertStatus(302, response);
        operateUser.refresh();
        assertEquals("test-userName", operateUser.userName);

    }


}
