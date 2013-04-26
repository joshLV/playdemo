package function;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.operator.OperateRole;
import models.operator.OperateUser;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.Map;

/**
 * User: wangjia
 * Date: 12-11-30
 * Time: 上午9:33
 */
public class OperateUsersProfilesTest extends FunctionalTest {
    OperateUser operateUser;
    OperateRole role;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
//        role = FactoryBoy.create(OperateRole.class);

        operateUser = FactoryBoy.create(OperateUser.class, new BuildCallback<OperateUser>() {
            @Override
            public void build(OperateUser ou) {
                ou.mobile = "13211111111";
            }
        });


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
    public void testIndex() {
        Http.Response response = GET("/profile");
        assertStatus(200, response);
        assertEquals(operateUser.roles.size(), ((OperateUser) renderArgs("operateUser")).roles.size());
    }

    @Test
    public void testIndexRolesNull() {
        operateUser.roles = null;
        operateUser.save();
        Http.Response response = GET("/profile");
        assertStatus(200, response);
        assertEquals(0, ((OperateUser) renderArgs("operateUser")).roles.size());
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

    @Test
    public void testUpdateInvalidOperateUser() {
        Map<String, String> params = new HashMap<>();
        params.put("operateUser.id", operateUser.id.toString());
        params.put("operateUser.loginName", "test-loginName");
        params.put("operateUser.userName", "test-userName");
        params.put("operateUser.mobile", "1390");
        params.put("operateUser.email", operateUser.email);
        params.put("operateUser.jobNumber", operateUser.jobNumber);
        params.put("operateUser.encryptedPassword", "123456");
        Http.Response response = POST("/profile", params);
        assertStatus(200, response);
        assertNotNull((OperateUser) renderArgs("operateUser"));
        assertEquals("1390", ((OperateUser) renderArgs("operateUser")).mobile);
    }


}
