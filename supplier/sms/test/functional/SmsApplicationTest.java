package functional;

import controllers.SmsApplication;
import org.junit.Test;
import play.mvc.Controller;
import play.mvc.Http;
import play.test.FunctionalTest;

/**
 * User: tanglq
 * Date: 13-1-6
 * Time: 上午11:15
 */
public class SmsApplicationTest extends FunctionalTest {

    @Test
    public void testGetIndex() throws Exception {
        Http.Response response = GET("/");
        assertIsOk(response);
    }

    @Test
    public void testInstance() throws Exception {
        assertEquals(true, (new SmsApplication) instanceof Controller);
    }
}
