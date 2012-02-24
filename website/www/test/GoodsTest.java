import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.test.FunctionalTest;

/**
 * 商品控制器的测试.
 * <p/>
 * User: sujie
 * Date: 2/24/12
 * Time: 5:22 PM
 */
public class GoodsTest extends FunctionalTest {
    @Test
    public void testShow() {
        Http.Response response = GET("/goods/1");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(Play.defaultWebEncoding, response);
        assertHeaderEquals("title", "优惠啦 - 商品详情", response);
    }

}
