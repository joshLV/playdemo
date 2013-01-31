package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.admin.OperateUser;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

/**
 * User: tanglq
 * Date: 13-1-31
 * Time: 下午2:55
 */
public class QuickSearchsTest extends FunctionalTest {

    private static String baseUrl = Play.configuration.getProperty("application.baseUrl");
    @Before
    public void setUp() {
        FactoryBoy.lazyDelete();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @After
    public void tearDown() throws Exception {
        Security.cleanLoginUserForTest();
    }


    @Test
    public void testEmptyQuery() throws Exception {
        Http.Response response = GET(Router.reverse("QuickSearchs.query").url);
        assertEquals("请输入搜索参数", getContent(response));
    }

    @Test
    public void 无法处理的参数() throws Exception {
        String value = "3294234923423423423234234234";
        Http.Response response = GET(Router.reverse("QuickSearchs.query").url + "?q=" + value);
        assertEquals("没有找到" + value + "相关的信息，请关闭重试.", getContent(response));
    }

    @Test
    public void testPhone() throws Exception {
        String value = "13026682165";
        Http.Response response = GET(Router.reverse("QuickSearchs.query").url + "?q=" + value);
        assertHeaderEquals("Location", baseUrl + "/orders?condition.searchKey=MOBILE&condition.searchItems=" + value,
                response);
    }
}
