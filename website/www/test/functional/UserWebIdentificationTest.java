package functional;

import java.util.HashMap;
import java.util.Map;
import models.consumer.User;
import models.consumer.UserWebIdentification;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http.Cookie;
import play.mvc.Http.Header;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import controllers.WebsiteInjector;

public class UserWebIdentificationTest extends FunctionalTest {

    @Before
    public void setup() {
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(User.class);
        Fixtures.loadModels("fixture/user.yml");
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
   
        UserWebIdentification uwi = UserWebIdentification.find("byCookieId", cookie.value).first();
        assertNotNull(uwi);
        assertNotNull(uwi.firstPage);
        assertNull(uwi.user);
    }
    

    @Test
    public void testReferCode() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-goods4");
        Response response = GET("/g/" + goodsId + "?tj=JKKIFESFDF");
        
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
        
        Cookie cookie = response.cookies.get(WebsiteInjector.WEB_TRACK_COOKIE);
        assertNotNull(cookie);
   
        UserWebIdentification uwi = UserWebIdentification.find("byCookieId", cookie.value).first();
        assertNotNull(uwi);
        assertNull(uwi.user);
        assertNotNull(uwi.firstPage);
        assertNotNull(uwi.referCode);
    }    

    @Test
    public void testRefererPageWorks() {
        Header header = new Header("Referer", "http://www.google.com/search?s=xxx");
        Map<String, Header> headers = new HashMap<>();
        headers.put("Referer", header);
        Request request = Request.createRequest(
                null,
                "GET",
                "/",
                "",
                null,
                null,
                null,
                null,
                false,
                80,
                "localhost",
                false,
                headers,
                null
        );;
        Response response = GET(request, "/");
        
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
        
        Cookie cookie = response.cookies.get(WebsiteInjector.WEB_TRACK_COOKIE);
        assertNotNull(cookie);
   
        UserWebIdentification uwi = UserWebIdentification.find("byCookieId", cookie.value).first();
        assertNotNull(uwi);
        assertNotNull(uwi.referer);
        assertNotNull(uwi.refererHost);
        assertEquals("www.google.com", uwi.refererHost);
    }
    
    @Test
    public void testMatchHostName() throws Exception {
        assertEquals("www.google.com", WebsiteInjector.matchTheHostName("http://www.google.com/search?s=xxx"));
        assertEquals("localhost:8080", WebsiteInjector.matchTheHostName("http://localhost:8080/search?s=xxx"));
        assertEquals("www.google.com", WebsiteInjector.matchTheHostName("https://www.google.com/search?s=xxx"));
        assertEquals("localhost:8080", WebsiteInjector.matchTheHostName("https://localhost:8080/search?s=xxx"));
        
    }

}
