package functional;

import controllers.YihaodianAPI;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 * Date: 13-1-9
 */
public class YihaodianAPITest extends FunctionalTest{
    @Before
    public void setup() {
        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testConstractor() {
        assertTrue(new YihaodianAPI() instanceof Controller);
    }
    @Test
    public void testIndex() {
        Http.Response response = GET(Router.reverse("controllers.YihaodianAPI.index"));
        assertIsOk(response);
    }

    @Test
    public void testSend() {
        Map<String, String> params = new HashMap<>();
        params.put("paramStr", "abc::def");
        Http.Response response = POST(Router.reverse("controllers.YihaodianAPI.request"), params);
        assertIsOk(response);
    }

}
