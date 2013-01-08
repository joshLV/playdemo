package function;

import controllers.modules.resale.cas.Security;
import factory.FactoryBoy;
import models.resale.Resaler;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.util.HashMap;
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

    }

    @Test
    public void testIndex() throws Exception {
        Http.Response response = GET("/resaler/edit-password");
        assertIsOk(response);
        assertEquals(resaler, renderArgs("resaler"));
        assertEquals("", ((Resaler) renderArgs("resaler")).password);
    }

    @Test
    public void testUpdatePassword() throws Exception {
        resaler.passwordSalt = "abcd";
        resaler.password = DigestUtils.md5Hex("1" + resaler.passwordSalt);
        resaler.save();

        Map<String, String> params = new HashMap<>();
        params.put("resaler.oldPassword", "1");
        params.put("resaler.password", "123456");
        params.put("resaler.confirmPassword", "123456");
        Http.Response response = POST("/resaler/edit-password", params);
        assertIsOk(response);
        assertEquals("isOk", renderArgs("isOk"));
    }
}
