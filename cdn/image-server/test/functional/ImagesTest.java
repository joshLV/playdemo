package functional;

import com.uhuila.common.util.PathUtil;
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
    public void testInvalidSign() {
        Http.Response response = GET("/p/1/1/1/origin.jpg");
        assertIsNotFound(response);

        response = GET("/p/1/1/1/abc_origin.jpg");
        assertIsNotFound(response);
    }

    @Test
    @Ignore
    public void testDefaultImg(){
        Http.Response response = GET(PathUtil.imgSign("/p/1/1/1/none.jpg"));
        assertIsOk(response);
        assertContentType("image/jpeg", response);
    }

    @Test
    @Ignore
    public void testShowImageWithoutWatermark() {
        Http.Response response = GET(PathUtil.imgSign("/p/1/1/1/origin_nw.jpg"));
        assertIsOk(response);
        assertContentType("image/jpeg", response);
    }

    @Test
    @Ignore
    public void testShowResizeImage() {
        Http.Response response = GET(PathUtil.imgSign("/p/1/1/1/origin_100x100.jpg"));
        assertIsOk(response);
        assertContentType("image/jpeg", response);
    }

}