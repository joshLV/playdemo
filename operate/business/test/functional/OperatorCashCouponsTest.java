package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.admin.OperateUser;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-9-26
 */
@Ignore
public class OperatorCashCouponsTest extends FunctionalTest{
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

    @Test
    public void testIndex(){
        Http.Response response = GET("/cash-coupon");
        assertIsOk(response);
    }

    @Test
    public void testGenerator(){
        Http.Response response = GET("/cash-coupon/generator");
        assertIsOk(response);
    }

    @Test
    public void testGenerate(){
        Map<String, String> params = new HashMap<>();
        params.put("faceValue", "");
        params.put("count", "");
        params.put("name", "");
        params.put("prefix", "");

        Http.Response response = POST("/cash-coupon/generate", params);
        assertIsOk(response);
    }
}
