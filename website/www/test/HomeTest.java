import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

public class HomeTest extends FunctionalTest {

    @Before
    public void setup() {
        Fixtures.delete(Goods.class);
        Fixtures.loadModels("goods.yml");
    }

    @Test
    public void testThatIndexPageWorks() {
        Response response = GET("/");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }

}