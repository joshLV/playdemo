package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.WebSqlCommand;
import models.operator.OperateUser;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.mvc.Router;
import play.mvc.Scope;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import util.mq.MockMQ;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebSqlExcutorsTest extends FunctionalTest {


    OperateUser operateUser;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        MockMQ.clear();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        operateUser = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);

        FactoryBoy.create(WebSqlCommand.class);

    }

    /**
     * 设置安全码.
     */
    private void inputSecretKey() {
        Scope.Session.current.set(new Scope.Session());
        Map<String, String> params = new HashMap<>();
        params.put("secret", "test");
        POST(Router.reverse("WebSqlExcutors.secret").url, params);
    }
    @Test
    public void testIndexNoSecret() throws Exception {
        Http.Response response = GET(Router.reverse("WebSqlExcutors.index").url);
        assertStatus(302, response);
    }

    @Test
    public void testIndex() throws Exception {
        inputSecretKey();
        Http.Response response = GET(Router.reverse("WebSqlExcutors.index").url);
        assertIsOk(response);
    }

    @Test
    public void testRunSelect() throws Exception {
        inputSecretKey();
        Map<String, String> params = new HashMap<>();
        params.put("sql", "select * from \"operate_users\"");
        Http.Response response = POST(Router.reverse("WebSqlExcutors.run").url, params);
        assertIsOk(response);
        String message = (String) renderArgs("message");
        assertNull(message);
        List<String> columnNames = (List<String>) renderArgs("columnNames");
        assertTrue(columnNames.size() > 0);
    }

    @Test
    public void testRunSelectWebSQLCommand() throws Exception {
        inputSecretKey();
        Map<String, String> params = new HashMap<>();
        params.put("sql", "select * from \"web_sql_commands\"");
        Http.Response response = POST(Router.reverse("WebSqlExcutors.run").url, params);
        assertIsOk(response);
        String message = (String) renderArgs("message");
        assertEquals("不允许操作『web_sql_commands』表。", message);
    }

    @Test
    public void testRunUpdateWithoutRemark() throws Exception {
        inputSecretKey();
        Map<String, String> params = new HashMap<>();
        params.put("sql", "delete from \"operate_users_roles\"");
        Http.Response response = POST(Router.reverse("WebSqlExcutors.run").url, params);
        assertIsOk(response);
        String message = (String) renderArgs("message");
        assertEquals("修改数据库的操作需要输入『备注』信息。", message);
    }

    @Test
    public void testRunDelete() throws Exception {
        inputSecretKey();
        Map<String, String> params = new HashMap<>();
        params.put("sql", "delete from \"operate_users_roles\"");
        params.put("remark", "delete");
        Http.Response response = POST(Router.reverse("WebSqlExcutors.run").url, params);
        assertIsOk(response);
        String message = (String) renderArgs("message");
        assertContentMatch("执行DELETE操作，影响\\d+条记录。", response);

    }

    @Test
    public void testHistory() throws Exception {
        inputSecretKey();
        Http.Response response = GET(Router.reverse("WebSqlExcutors.history").url);
        assertIsOk(response);
        JPAExtPaginator<WebSqlCommand> sqlPage = (JPAExtPaginator<WebSqlCommand>) renderArgs("sqlPage");
        assertEquals(1, sqlPage.getRowCount());
    }

    /**
     * 安全码输入界面
     * @throws Exception
     */
    @Test
    public void testSecret() throws Exception {
        Http.Response response = GET(Router.reverse("WebSqlExcutors.secret").url);
        assertIsOk(response);
        assertContentMatch("请输入SQL执行器安全码", response);
    }
}
