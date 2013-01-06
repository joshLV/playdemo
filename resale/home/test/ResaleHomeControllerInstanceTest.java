import controllers.ResalerInfos;
import org.junit.Test;
import play.mvc.Controller;
import play.test.UnitTest;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 13-1-6
 * Time: 上午11:41
 * To change this template use File | Settings | File Templates.
 */
public class ResaleHomeControllerInstanceTest extends UnitTest {
    @Test
    public void controllerInstanceTest() {
        assertTrue(new ResalerInfos() instanceof Controller);
    }
}
