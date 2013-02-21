package function;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import play.data.validation.Error;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运营后台帐号的密码管理的功能测试.
 * <p/>
 * User: sujie
 * Date: 1/10/13
 * Time: 9:49 AM
 */
public class OperateUsersPasswordTest extends FunctionalTest {
    OperateUser user;

    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();
        user = FactoryBoy.create(OperateUser.class);
        user.passwordSalt = "1234";
        user.encryptedPassword = DigestUtils.md5Hex("111111" + user.passwordSalt);
        user.save();
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testIndex() {
        Http.Response response = GET(Router.reverse("OperateUsersPassword.index").url);
        assertIsOk(response);
    }

    @Test
    public void testUpdate() {
        Map<String, String> params = new HashMap<>();
        params.put("password", "123456");
        params.put("confirmPassword", "123456");
        params.put("oldPassword", "111111");
        Http.Response response = POST(Router.reverse("OperateUsersPassword.update"), params);
        assertIsOk(response);
        assertTrue((Boolean) renderArgs("isOk"));

        user.refresh();
        // 加密后的原密码比较
        String oldPassword = DigestUtils.md5Hex("123456" + user.passwordSalt);
        assertEquals(oldPassword, user.encryptedPassword);
    }


    public void testUpdateInvalid_NewPwd() {
        Map<String, String> params = new HashMap<>();
        params.put("confirmPassword", "123456");
        params.put("oldPassword", "111111");
        Http.Response response = POST(Router.reverse("OperateUsersPassword.update"), params);

        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("operateUser.newPassword", errors.get(0).getKey());
        assertIsOk(response);
    }

    @Test
    public void testUpdateInvalid_ConfirmPwd() {
        Map<String, String> params = new HashMap<>();
        params.put("password", "123456");
        params.put("oldPassword", "111111");
        Http.Response response = POST(Router.reverse("OperateUsersPassword.update"), params);

        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("operateUser.confirmPassword", errors.get(0).getKey());
        assertIsOk(response);
    }

    @Test
    public void testUpdateInvalid_PwdShortLength() {
        Map<String, String> params = new HashMap<>();
        params.put("password", "12345");
        params.put("confirmPassword", "12345");
        params.put("oldPassword", "111111");
        Http.Response response = POST(Router.reverse("OperateUsersPassword.update"), params);

        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("operateUser.newPassword", errors.get(0).getKey());
        assertIsOk(response);
    }

    @Test
    public void testUpdateInvalid_PwdLongLength() {
        Map<String, String> params = new HashMap<>();
        params.put("password", "12345456789012345678901");
        params.put("confirmPassword", "12345456789012345678901");
        params.put("oldPassword", "111111");
        Http.Response response = POST(Router.reverse("OperateUsersPassword.update"), params);

        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("operateUser.newPassword", errors.get(0).getKey());
        assertIsOk(response);
    }

    @Test
    public void testUpdateInvalid_ConfirmDiff() {
        Map<String, String> params = new HashMap<>();
        params.put("password", "123454");
        params.put("confirmPassword", "123456");
        params.put("oldPassword", "111111");
        Http.Response response = POST(Router.reverse("OperateUsersPassword.update"), params);

        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("operateUser.confirmPassword", errors.get(0).getKey());
        assertIsOk(response);
    }

    @Test
    public void testUpdateInvalid_OldPwd() {
        Map<String, String> params = new HashMap<>();
        params.put("password", "123456");
        params.put("confirmPassword", "123456");
        params.put("oldPassword", "111");
        Http.Response response = POST(Router.reverse("OperateUsersPassword.update"), params);

        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("operateUser.oldPassword", errors.get(0).getKey());
        assertIsOk(response);
    }

    @Test
    public void testUpdateInvalid_SamePwd() {
        Map<String, String> params = new HashMap<>();
        params.put("password", "111111");
        params.put("confirmPassword", "111111");
        params.put("oldPassword", "111111");
        Http.Response response = POST(Router.reverse("OperateUsersPassword.update"), params);

        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("operateUser.newPassword", errors.get(0).getKey());
        assertIsOk(response);
    }
}
    