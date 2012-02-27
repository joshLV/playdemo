import models.consumer.Address;
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
        Fixtures.loadModels("initial-data.yml");
    }

    @Test
    public void testThatIndexPageWorks() {
        Response response = GET("/");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
        assertHeaderEquals("title", "优惠啦 - 首页", response);

        //todo 测试商品的筛选

    }

}