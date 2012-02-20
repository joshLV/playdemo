import org.junit.Test;

import play.mvc.Http.Response;
import play.test.FunctionalTest;


public class OrderTest extends FunctionalTest {
	 @Test
	    public void testQuery() {
	        Response response = GET("/");
	        assertIsOk(response);
	        assertContentType("text/html", response);
	        assertCharset(play.Play.defaultWebEncoding, response);
	    }
}
