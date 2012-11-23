package functional;

import models.sales.Goods;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.FunctionalTest;
import factory.FactoryBoy;

public class HomeTest extends FunctionalTest {
    Goods goods;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        goods = FactoryBoy.create(Goods.class);
    }

    @Test
    public void testThatIndexPageWorks() {
        Response response = GET("/");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }

}