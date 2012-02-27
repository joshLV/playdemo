package unit;

import models.sales.Goods;
import org.junit.Test;
import play.test.UnitTest;

import static junit.framework.Assert.assertEquals;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 2/27/12
 * Time: 5:59 PM
 */
public class GoodsUnitTest extends UnitTest {
    @Test
    public void testGetImageBySizeType() {
        models.sales.Goods goods = new Goods();
        goods.imagePath = "/1/1/1/3.jpg";
        String path = goods.getImageLargePath();
        assertEquals("http://img0.uhlcdndev.net/p/1/1/1/3_large.jpg", path);
    }

}
