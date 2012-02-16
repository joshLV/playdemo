import models.sales.Goods;
import org.junit.Test;
import play.test.UnitTest;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 2/13/12
 * Time: 3:54 PM
 */
public class GoodsTest
    extends UnitTest {

    @Test
    public void testShowImage() throws Exception {
        Goods goods = new Goods();
        goods.imagePath = "/o/1/1/1/3.jpg";
        String path = goods.getImageMiddlePath();
        assertEquals("/p/1/1/1/3_middle.jpg", path);
    }

}
