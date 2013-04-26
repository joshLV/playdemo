package function;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
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
    String[] roleText = {"销售人员", "系统管理员", "测试角色", "编辑", "客服", "网站运营", "开发人员", "经理", "财务", "虚拟验证", "库存管理员"};
    String[] roleKey = {"sales", "admin", "test", "editor", "customservice", "webop", "developer", "manager", "account", "virtual_verify", "inventory_manager"};
    int index=0;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        FactoryBoy.batchCreate(11, OperateRole.class,
                new SequenceCallback<OperateRole>() {
                    @Override
                    public void sequence(OperateRole role, int seq) {
                        role.text = roleText[index];
                        role.key = roleKey[index++];
                    }
                });



        operateUser = FactoryBoy.create(OperateUser.class, "role");


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
