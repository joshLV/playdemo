package functional;

import java.util.HashMap;
import java.util.Map;

import models.consumer.User;
import models.consumer.UserWebIdentification;
import models.sales.Goods;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Cookie;
import play.mvc.Http.Header;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import controllers.WebsiteInjector;
import factory.FactoryBoy;

public class UserWebIdentificationTest extends FunctionalTest {
    User user;
    Goods goods;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        user = FactoryBoy.create(User.class);
        goods = FactoryBoy.create(Goods.class);

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
        Response response = GET("/g/" + goods.id + "?tj=JKKIFESFDF");

        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);

        Cookie cookie = response.cookies.get(WebsiteInjector.WEB_TRACK_COOKIE);
        assertNotNull(cookie);

        // 已经是通过MQ保存，直接是找不到的。
        /*
        UserWebIdentification uwi = UserWebIdentification.find("byCookieId", cookie.value).first();
        assertNotNull(uwi);
        assertNull(uwi.user);
        assertNotNull(uwi.firstPage);
        assertNotNull(uwi.referCode);
        */
    }

    @Test
    public void testRefererPageWorks() {
        Header header = new Header("referer", "http://www.google.com/search?s=xxx");
        Map<String, Header> headers = new HashMap<>();
        headers.put("referer", header);
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
        );
        ;
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
