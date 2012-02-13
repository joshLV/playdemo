import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

/**
 * 图片Controller的测试类.
 * <p/>
 * User: sujie
 * Date: 2/8/12
 * Time: 11:33 AM
 */
public class ImagesTest extends FunctionalTest {

    @Test
    public void testShowImage() {
        Http.Response response = GET("/p/1/1/1/origin_100x100.jpg");
        assertIsNotFound(response);
    }
    @Test
    public void testShowImageBySmall() {
        Http.Response response = GET("/p/1/1/1/origin_small.jpg");
        assertIsOk(response);
        assertContentType("image/jpeg", response);
    }

    @Test
    public void testShowImageByMiddle() {
        Http.Response response = GET("/p/1/1/1/origin_middle.jpg");
        assertIsOk(response);
        assertContentType("image/jpeg", response);
    }

    @Test
    public void testShowImageByLarge() {
        Http.Response response = GET("/p/1/1/1/origin_large.jpg");
        assertIsOk(response);
        assertContentType("image/jpeg", response);
    }

    @Test
    public void testShowImageByIllegal() {
        Http.Response response = GET("/p/1/1/1/origin.jpg");
        assertIsNotFound(response);
    }
}