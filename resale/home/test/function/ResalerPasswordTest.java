package function;

import controllers.modules.resale.cas.Security;
import factory.FactoryBoy;
import models.resale.Resaler;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import play.data.validation.Error;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 13-1-8
 * Time: 下午1:36
 */
public class ResalerPasswordTest extends FunctionalTest {
    Resaler resaler;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        resaler = FactoryBoy.create(Resaler.class);
        Security.setLoginUserForTest(resaler.loginName);
        resaler.oldPassword = "111111";
        resaler.passwordSalt = "abcd";
        resaler.password = DigestUtils.md5Hex("111111" + resaler.passwordSalt);
        resaler.save();
    }

    @Test
    public void testIndex() throws Exception {
        Http.Response response = GET("/resaler/edit-password");
        assertIsOk(response);
        assertContentMatch("修改密码", response);
    }

    @Test
    public void testUpdatePassword() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("oldPassword", "111111");
        params.put("password", "123456");
        params.put("confirmPassword", "123456");
        Http.Response response = POST("/resaler/edit-password", params);
        assertIsOk(response);
        assertTrue((Boolean) renderArgs("isOk"));

        resaler.refresh();
        // 加密后的原密码比较
        String oldPassword = DigestUtils.md5Hex("123456" + resaler.passwordSalt);
        assertEquals(oldPassword, resaler.password);
    }


    public void testUpdateInvalid_NewPwd() {
        Map<String, String> params = new HashMap<>();
        params.put("confirmPassword", "123456");
        params.put("oldPassword", "111111");
        Http.Response response = POST("/resaler/edit-password", params);

        List<Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("newPassword", errors.get(0).getKey());
        assertIsOk(response);
    }

    @Test
    public void testUpdateInvalid_ConfirmPwd() {
        Map<String, String> params = new HashMap<>();
        params.put("password", "123456");
        params.put("oldPassword", "111111");
        Http.Response response = POST("/resaler/edit-password", params);

        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("resaler.confirmPassword", errors.get(0).getKey());
        assertIsOk(response);
    }

    @Test
    public void testUpdateInvalid_PwdShortLength() {
        Map<String, String> params = new HashMap<>();
        params.put("password", "12345");
        params.put("confirmPassword", "12345");
        params.put("oldPassword", "111111");
        Http.Response response = POST("/resaler/edit-password", params);
        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("resaler.newPassword", errors.get(0).getKey());
        assertIsOk(response);
    }

    @Test
    public void testUpdateInvalid_PwdLongLength() {
        Map<String, String> params = new HashMap<>();
        params.put("password", "12345456789012345678901");
        params.put("confirmPassword", "12345456789012345678901");
        params.put("oldPassword", "111111");
        Http.Response response = POST("/resaler/edit-password", params);

        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("resaler.newPassword", errors.get(0).getKey());
        assertIsOk(response);
    }

    @Test
    public void testUpdateInvalid_ConfirmDiff() {
        Map<String, String> params = new HashMap<>();
        params.put("password", "123454");
        params.put("confirmPassword", "123456");
        params.put("oldPassword", "111111");
        Http.Response response = POST("/resaler/edit-password", params);

        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("resaler.confirmPassword", errors.get(0).getKey());
        assertIsOk(response);
    }

    @Test
    public void testUpdateInvalid_OldPwd() {
        Map<String, String> params = new HashMap<>();
        params.put("password", "123456");
        params.put("confirmPassword", "123456");
        params.put("oldPassword", "111");
        Http.Response response = POST("/resaler/edit-password", params);

        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("resaler.oldPassword", errors.get(0).getKey());
        assertIsOk(response);
    }

    @Test
    public void testUpdateInvalid_SamePwd() {
        Map<String, String> params = new HashMap<>();
        params.put("password", "111111");
        params.put("confirmPassword", "111111");
        params.put("oldPassword", "111111");
        Http.Response response = POST("/resaler/edit-password", params);
        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("resaler.newPassword", errors.get(0).getKey());
        assertIsOk(response);
    }

}
