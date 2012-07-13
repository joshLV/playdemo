package functional;

import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http.Cookie;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import controllers.WebsiteInjector;

public class UserWebIdentificationTest extends FunctionalTest {

    @Before
    public void setup() {
        Fixtures.delete(Goods.class);
        Fixtures.loadModels("fixture/goods.yml");
    }

    @Test
    public void testThatIndexPageWorks() {
        Response response = GET("/");
        
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
        
        Cookie cookie = response.cookies.get(WebsiteInjector.WEB_TRACK_COOKIE);
        assertNotNull(cookie);
    }

}
