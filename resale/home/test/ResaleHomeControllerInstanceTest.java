import controllers.ResalerFavs;
import controllers.ResalerInfos;
import org.junit.Test;
import play.mvc.Controller;
import play.test.UnitTest;

/**
 * User: wangjia
 * Date: 13-1-6
 * Time: 上午11:41
 */
public class ResaleHomeControllerInstanceTest extends UnitTest {
    @Test
    public void controllerInstanceTest() {
        assertTrue(new ResalerInfos() instanceof Controller);
        assertTrue(new ResalerFavs() instanceof Controller);
    }
}
