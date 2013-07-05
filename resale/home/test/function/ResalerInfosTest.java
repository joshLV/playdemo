package function;

import controllers.modules.resale.cas.Security;
import factory.FactoryBoy;
import models.resale.Resaler;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User: wangjia
 * Date: 13-1-6
 * Time: 上午9:41
 */
public class ResalerInfosTest extends FunctionalTest {
    Resaler resaler;

    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();
        resaler = FactoryBoy.create(Resaler.class);
        Security.setLoginUserForTest(resaler.loginName);
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/resaler-info");
        assertStatus(200, response);
        assertEquals(resaler, (Resaler) renderArgs("resaler"));
    }

    @Test
    public void testUpdateInvalid() {
        Map<String, String> params = new HashMap<>();
        params.put("resaler.address", resaler.address);
        params.put("resaler.mobile", "1390");
        params.put("resaler.phone", resaler.phone);
        params.put("resaler.email", resaler.email);
        params.put("resaler.userName", resaler.userName);
        params.put("resaler.postCode", resaler.postCode);
        params.put("resaler.updatedAt", new Date().toString());
        Http.Response response = POST("/resaler-info", params);
        assertStatus(302, response);

    }


    @Test
    public void testUpdate() {
        assertEquals("13000000001", resaler.mobile);
        Map<String, String> params = new HashMap<>();
        params.put("resaler.address", resaler.address);
        params.put("resaler.mobile", "13987678909");
        params.put("resaler.phone", resaler.phone);
        params.put("resaler.email", resaler.email);
        params.put("resaler.userName", resaler.userName);
        params.put("resaler.resalerName", resaler.userName);
        params.put("resaler.postCode", resaler.postCode);
        params.put("resaler.loginName", resaler.loginName);
        params.put("resaler.password", resaler.password);
        params.put("resaler.confirmPassword", resaler.confirmPassword);
        params.put("resaler.identityNo", resaler.identityNo);
        Http.Response response = POST("/resaler-info", params);
        assertStatus(200, response);
        assertEquals("13987678909", ((Resaler) renderArgs("resaler")).mobile);
        resaler.refresh();
        assertEquals("13987678909", resaler.mobile);


    }

}
