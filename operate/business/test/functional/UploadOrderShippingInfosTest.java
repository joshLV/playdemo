package functional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Play;
import play.test.FunctionalTest;

import java.io.File;

/**
 * <p/>
 * User: yanjy
 * Date: 13-3-15
 * Time: 上午9:50
 */
public class UploadOrderShippingInfosTest extends FunctionalTest {

    @BeforeClass
    public static void setUpClass() {
        Play.tmpDir = new File("/tmp"); //解决测试时上传失败的问题
    }

    @AfterClass
    public static void tearDownClass() {
        Play.tmpDir = null;
    }
@Test
    public void testIndex(){

}
}
