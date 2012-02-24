import org.junit.Test;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

public class HomeTest extends FunctionalTest {

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