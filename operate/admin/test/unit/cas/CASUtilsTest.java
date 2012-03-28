package unit.cas;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import play.Play;
import play.operate.cas.CASUtils;
import play.operate.cas.models.CASUser;
import play.mvc.Http;
import play.test.UnitTest;

public class CASUtilsTest extends UnitTest {


    @Before
    public void setUp() {
        Play.configuration.setProperty("cas.gateway", "false");
        Play.configuration.setProperty("cas.mockserver", "true");

        Play.configuration.setProperty("cas.validateUrl", "http://cas.seewi.com.cn/serviceValidate");
        Play.configuration.setProperty("cas.loginUrl", "http://cas.seewi.com.cn/login");
        Play.configuration.setProperty("cas.logoutUrl", "http://cas.seewi.com.cn/logout");
        Play.configuration.setProperty("application.baseUrl", "http://localhost:9301");

        Http.Request.current.set(Http.Request.createRequest("127.0.0.1", "GET", "/login", null, null, null, null, "admin.seewi.com.cn", false, 80, "admin.seewi.com.cn", false, null, null));
    }
    
    @After
    public void tearDown() {
        Play.configuration.setProperty("cas.gateway", "true");
        Play.configuration.setProperty("cas.mockserver", "true");
    }


    @Test
    public void testGetDomainName(){
        String casUrlTemp = "http://cas.seewi.com.cn/login";
        assertEquals("http://cas.seewi.com.cn/login", casUrlTemp);
    }

    @Test
    public void getCasLoginUrlTest1() {
        Play.configuration.setProperty("cas.mockserver", "false");
        String casLoginUrl = CASUtils.getCasLoginUrl(Boolean.TRUE);
        assertEquals("http://cas.seewi.com.cn/login?service=http://admin.seewi.com.cn/authenticate", casLoginUrl);

        Play.configuration.setProperty("cas.gateway", "true");
        casLoginUrl = CASUtils.getCasLoginUrl(Boolean.TRUE);
        assertEquals(
                "http://cas.seewi.com.cn/login?service=http://admin.seewi.com.cn/authenticate&gateway=true",
                casLoginUrl);
    }

    @Test
    public void getCasLoginUrlTest2() {
        Play.configuration.setProperty("cas.mockserver", "false");
        String casLoginUrl = CASUtils.getCasLoginUrl(Boolean.FALSE);
        assertEquals("http://cas.seewi.com.cn/login?service=http://admin.seewi.com.cn/authenticate", casLoginUrl);
        casLoginUrl = CASUtils.getCasLoginUrl(Boolean.TRUE);
        assertEquals(
                "http://cas.seewi.com.cn/login?service=http://admin.seewi.com.cn/authenticate",
                casLoginUrl);
    }

    @Test
    public void getCasLogoutUrlTest() {
        Play.configuration.setProperty("cas.mockserver", "false");
        String casLogoutUrl = CASUtils.getCasLogoutUrl();
        assertEquals("http://cas.seewi.com.cn/logout", casLogoutUrl);
    }

    @Test
    public void getCasLogoutUrlTest2() {
        String casLogoutUrl = CASUtils.getCasLogoutUrl();
        assertEquals("http://admin.seewi.com.cn/@cas/logout", casLogoutUrl);
    }

    @Test
    public void isMockSeverTest1() {
        assertTrue(CASUtils.isCasMockServer());
    }

    @Test
    public void isMockSeverTest2() {
        Play.configuration.setProperty("cas.mockserver", "false");
        assertFalse(CASUtils.isCasMockServer());
    }

}
