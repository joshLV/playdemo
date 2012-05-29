package functional;

import org.junit.Test;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

public class AppVersionTest  extends FunctionalTest {

    @Test
    public void testVersionPage() {
        Response response = GET("/@appversion");
        assertContentMatch("website-www", response);
    }    
    
}
