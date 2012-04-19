package unit.cas;

import navigation.NavigationHandler;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.test.UnitTest;

public class NavigationHandlerTest extends UnitTest {


    @Before
    public void setUp() {
        Play.configuration.setProperty("cas.gateway", "false");
        Play.configuration.setProperty("cas.mockserver", "true");

        Play.configuration.setProperty("cas.validateUrl", "http://{domain}.cas.uhuila.net/serviceValidate");
        Play.configuration.setProperty("cas.loginUrl", "http://{domain}.cas.uhuila.net/login");
        Play.configuration.setProperty("cas.logoutUrl", "http://{domain}.cas.uhuila.net/logout");
        Play.configuration.setProperty("application.baseUrl", "http://{domain}.order.uhuila.net");

        Http.Request.current.set(Http.Request.createRequest("127.0.0.1", "GET", "/login", null, null, null, null, "lyf.order.uhuila.net", false, 80, "lyf.order.uhuila.net", false, null, null));
    }


    @Test
    public void testGetOperatorProfileUrl() {
        assertEquals("http://lyf.home.uhuila.net/info", NavigationHandler.getSupplierInfoUrl());
        assertEquals("http://lyf.admin.uhuila.net/profile", NavigationHandler.getOperatorProfileUrl());
    }
    
    
}
