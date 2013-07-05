package function;

import factory.FactoryBoy;
import models.resale.AccountType;
import models.resale.Resaler;
import models.resale.ResalerLevel;
import models.resale.ResalerStatus;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterTest extends FunctionalTest {
    Resaler resaler;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        resaler = FactoryBoy.create(Resaler.class);
    }

    @Test
    public void testCreat() {
        List old = Resaler.findAll();
        int count = old.size();
        Map<String, String> loginUserParams = new HashMap<String,
                String>();
        //异常情况
        loginUserParams.put("resaler.loginName", "rrrr");
        loginUserParams.put("resaler.mobile", "13131121123");
        loginUserParams.put("resaler.password", "123456");
        loginUserParams.put("resaler.confirmPassword", "126");
        Response response = POST("/register", loginUserParams);
        assertStatus(200, response);

        loginUserParams.put("resaler.loginName", "2qqq");
        loginUserParams.put("resaler.mobile", "15131621121");
        loginUserParams.put("resaler.password", "1111111a");
        loginUserParams.put("resaler.confirmPassword", "1111111a");
        loginUserParams.put("resaler.address", "bb");
        loginUserParams.put("resaler.email", "112@qq.com");
        loginUserParams.put("resaler.accountType", AccountType.CONSUMER.toString());
        //正常
        loginUserParams.put("resaler.status", ResalerStatus.PENDING.toString());
        loginUserParams.put("resaler.phone", "0213112121");
        loginUserParams.put("resaler.userName", "aa");
        loginUserParams.put("resaler.resalerName", "aa");
        loginUserParams.put("resaler.identityNo", "341281198208268785");
        loginUserParams.put("resaler.postCode", "123456");
        loginUserParams.put("resaler.level", ResalerLevel.NORMAL.toString());
        response = POST("/register", loginUserParams);
        assertStatus(200, response);

        List newList = Resaler.findAll();
        assertEquals(count + 1, newList.size());
    }

    //测试是否存在用户名和手机
    @Test
    public void testCheckValue() {
        Map<String, String> loginUserParams = new HashMap<String,
                String>();
        loginUserParams.put("resaler.loginName", "qqq");
        loginUserParams.put("resaler.mobile", "13131121121");

        Response response = POST("/register/check-resaler", loginUserParams);
        assertStatus(200, response);
    }

}
