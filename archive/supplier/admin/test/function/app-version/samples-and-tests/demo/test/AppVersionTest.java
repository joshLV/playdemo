import org.junit.Test;
import play.AppVersion;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

public class AppVersionTest extends FunctionalTest {

    @Test
    public void testVersionValue() {
        assertEquals("512", AppVersion.revision);
        assertEquals("V1.0.0build1024", AppVersion.value);
        assertEquals("play-app-version-demo", AppVersion.name);
        assertNotNull(AppVersion.buildAt);
        assertNotNull(AppVersion.startupAt);
    }
 
    @Test
    public void testVersionPage() {
        Response response = GET("/@appversion");
        assertContentMatch("play-app-version-demo", response);
    }

}