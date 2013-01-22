import controllers.Addresses;
import controllers.SecKillGoodsController;
import org.junit.Test;
import play.mvc.Controller;
import play.test.UnitTest;

/**
 * User: wangjia
 * Date: 13-1-10
 * Time: 上午10:25
 */
public class WebsiteWwwControllerInstanceTest extends UnitTest {
    @Test
    public void controllerInstanceTest() {
        assertTrue(new Addresses() instanceof Controller);
        assertTrue(new SecKillGoodsController() instanceof Controller);
    }
}
