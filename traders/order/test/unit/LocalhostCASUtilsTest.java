package unit;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import play.Play;
import play.modules.cas.CASUtils;
import play.modules.cas.models.CASUser;
import play.mvc.Http;
import play.test.UnitTest;

public class LocalhostCASUtilsTest extends UnitTest {


    @Before
    public void setUp() {
        Play.configuration.setProperty("cas.gateway", "false");
        Play.configuration.setProperty("cas.mockserver", "false");

        Play.configuration.setProperty("cas.validateUrl", "http://{domain}.cas.supplierdev.com/serviceValidate");
        Play.configuration.setProperty("cas.loginUrl", "http://{domain}.cas.supplierdev.com/login");
        Play.configuration.setProperty("cas.logoutUrl", "http://{domain}.cas.supplierdev.com/logout");
        Play.configuration.setProperty("application.baseUrl", "http://{domain}.order.supplierdev.com");

        Http.Request.current.set(Http.Request.createRequest("127.0.0.1", "GET", "/login", null, null, null, null, "localhost", false, 8080, "localhost", false, null, null)); 
    }


    @Test
    public void testGetDomainName(){
        String casUrlTemp = "http://{domain}.cas.supplierdev.com/login";
        assertEquals("http://localhost.cas.supplierdev.com/login",
                     CASUtils.replaceCasUrl(casUrlTemp)
                     );

    }

    @Test
    public void getCasLoginUrlTest1() {
        Play.configuration.setProperty("cas.mockserver", "false");
        String casLoginUrl = CASUtils.getCasLoginUrl(Boolean.TRUE);
        assertEquals("http://localhost.cas.supplierdev.com/login?service=http://localhost:8080/modules.cas.securecas/authenticate", casLoginUrl);

        Play.configuration.setProperty("cas.gateway", "true");
        casLoginUrl = CASUtils.getCasLoginUrl(Boolean.TRUE);
        assertEquals(
                "http://localhost.cas.supplierdev.com/login?service=http://localhost:8080/modules.cas.securecas/authenticate&gateway=true",
                casLoginUrl);
    }

    @Test
    public void getCasLoginUrlTest2() {
        Play.configuration.setProperty("cas.mockserver", "false");
        String casLoginUrl = CASUtils.getCasLoginUrl(Boolean.FALSE);
        assertEquals("http://localhost.cas.supplierdev.com/login?service=http://localhost:8080/modules.cas.securecas/authenticate", casLoginUrl);
        casLoginUrl = CASUtils.getCasLoginUrl(Boolean.TRUE);
        assertEquals(
                "http://localhost.cas.supplierdev.com/login?service=http://localhost:8080/modules.cas.securecas/authenticate",
                casLoginUrl);
    }

    @Test
    public void getCasLogoutUrlTest() {
        Play.configuration.setProperty("cas.mockserver", "false");
        String casLogoutUrl = CASUtils.getCasLogoutUrl();
        assertEquals("http://localhost.cas.supplierdev.com/logout", casLogoutUrl);
    }

    @Test
    public void getCasLogoutUrlTest2() {
        Play.configuration.setProperty("cas.mockserver", "true");
        String casLogoutUrl = CASUtils.getCasLogoutUrl();
        assertEquals("http://localhost:8080/@cas/logout", casLogoutUrl);
    }

    @Test
    public void isMockSeverTest1() {
        Play.configuration.setProperty("cas.mockserver", "true");
        assertTrue(CASUtils.isCasMockServer());
    }

    @Test
    public void isMockSeverTest2() {
        Play.configuration.setProperty("cas.mockserver", "false");
        assertFalse(CASUtils.isCasMockServer());
    }

}
