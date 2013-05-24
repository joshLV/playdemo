package unit.cas;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.supplier.cas.CASUtils;
import play.test.UnitTest;

public class LocalhostCASUtilsTest extends UnitTest {


    @Before
    public void setUp() {
        Play.configuration.setProperty("cas.gateway", "false");
        Play.configuration.setProperty("cas.mockserver", "true");

        Play.configuration.setProperty("cas.validateUrl", "http://{domain}.cas.uhuila.net/serviceValidate");
        Play.configuration.setProperty("cas.loginUrl", "http://{domain}.cas.uhuila.net/login");
        Play.configuration.setProperty("cas.logoutUrl", "http://{domain}.cas.uhuila.net/logout");
        Play.configuration.setProperty("application.baseUrl", "http://{domain}.order.uhuila.net");

        Http.Request.current.set(Http.Request.createRequest("127.0.0.1", "GET", "/login", null, null, null, null, "localhost:8080", false, 8080, "localhost", false, null, null));
    }

    @After
    public void tearDown() {
        Play.configuration.setProperty("cas.gateway", "true");
        Play.configuration.setProperty("cas.mockserver", "true");
    }

    @Test
    public void testGetDomainName(){
        String casUrlTemp = "http://{domain}.cas.uhuila.net/login";
        assertEquals("http://localhost.cas.uhuila.net/login",
                     CASUtils.replaceCasUrl(casUrlTemp)
                     );

    }

    @Test
    public void getCasLoginUrlTest1() {
        Play.configuration.setProperty("cas.mockserver", "false");
        String casLoginUrl = CASUtils.getCasLoginUrl(Boolean.TRUE);
        assertEquals("http://localhost.cas.uhuila.net/login?service=http://localhost:8080/authenticate", casLoginUrl);

        Play.configuration.setProperty("cas.gateway", "true");
        casLoginUrl = CASUtils.getCasLoginUrl(Boolean.TRUE);
        assertEquals(
                "http://localhost.cas.uhuila.net/login?service=http://localhost:8080/authenticate&gateway=true",
                casLoginUrl);
    }

    @Test
    public void getCasLoginUrlTest2() {
        Play.configuration.setProperty("cas.mockserver", "false");
        String casLoginUrl = CASUtils.getCasLoginUrl(Boolean.FALSE);
        assertEquals("http://localhost.cas.uhuila.net/login?service=http://localhost:8080/authenticate", casLoginUrl);
        casLoginUrl = CASUtils.getCasLoginUrl(Boolean.TRUE);
        assertEquals(
                "http://localhost.cas.uhuila.net/login?service=http://localhost:8080/authenticate",
                casLoginUrl);
    }

    @Test
    public void getCasLogoutUrlTest() {
        Play.configuration.setProperty("cas.mockserver", "false");
        String casLogoutUrl = CASUtils.getCasLogoutUrl();
        assertEquals("http://localhost.cas.uhuila.net/logout", casLogoutUrl);
    }

    @Test
    public void getCasLogoutUrlTest2() {
        String casLogoutUrl = CASUtils.getCasLogoutUrl();
        assertEquals("http://localhost:8080/@cas/logout", casLogoutUrl);
    }


}
