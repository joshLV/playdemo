package function;

import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.admin.OperateRole;
import models.admin.OperateUser;
import operate.rbac.RbacLoader;
import org.apache.ivy.util.StringUtils;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User:yanjy
 * Time: 上午10:37
 */
public class OperateUsersTest extends FunctionalTest {
    OperateUser operateUser;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        operateUser = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/users?loginName=" + operateUser.loginName + "&userName=" + operateUser.userName +
                "&jobNumber" + operateUser.jobNumber);
        assertStatus(200, response);
        assertEquals(1, ((JPAExtPaginator<OperateUser>) renderArgs("operateUserPage")).size());
    }

    @Test
    public void testAdd() {
        Http.Response response = GET("/users/new");
        assertStatus(200, response);
        assertEquals(1, ((List<OperateRole>) renderArgs("rolesList")).size());
    }

    public static OperateRole role(String roleName) {
        OperateRole role = OperateRole.find("byKey", roleName).first();
        return role;
    }

    @Test
    public void testCreate() {
        assertEquals(1, OperateUser.count());
        Map<String, String> params = new HashMap<>();
        params.put("operateUser.encryptedPassword", "895623");
        params.put("operateUser.confirmPassword", "895623");
        params.put("operateUser.userName", "jim");
        params.put("operateUser.jobNumber", "123456");
        params.put("operateUser.mobile", operateUser.mobile);
        params.put("operateUser.loginName", "test-loginName");
        params.put("operateUser.email", "11@qq.com");
        params.put("operateUser.roles", StringUtils.join(operateUser.roles.toArray(), ","));
        Http.Response response = POST("/users", params);
        assertStatus(302, response);
        assertEquals(2, OperateUser.count());
    }


    @Test
    public void testCheckLoginName() {
        Map<String, String> params = new HashMap<>();
        params.put("id", operateUser.id.toString());
        params.put("operateUser.loginName", operateUser.loginName);
        params.put("operateUser.jobNumber", operateUser.jobNumber);
        Http.Response response = POST("/users/checkLoginName", params);
        assertContentType("application/json", response);
    }

    @Test
    public void testEdit() {
        Http.Response response = GET("/users/" + operateUser.id + "/edit");
        assertStatus(200, response);
        assertEquals(operateUser, ((OperateUser) (renderArgs("operateUser"))));
    }

    @Test
    public void testUpdate() {
        Map<String, String> params = new HashMap<>();
        params.put("operateUser.encryptedPassword", "895623");
        params.put("operateUser.confirmPassword", "895623");
        params.put("operateUser.id", operateUser.id.toString());
        params.put("operateUser.loginName", operateUser.loginName);
        params.put("operateUser.jobNumber", "9988");
        Http.Response response = POST("/users/" + operateUser.id, params);
        assertStatus(302, response);
        operateUser.refresh();
        assertEquals(operateUser.jobNumber, "9988");
    }

    @Test
    public void testDelete() {
        Http.Response response = DELETE("/users/" + operateUser.id);
        assertStatus(302, response);
        operateUser.refresh();
        assertEquals(DeletedStatus.DELETED, operateUser.deleted);
    }


}
