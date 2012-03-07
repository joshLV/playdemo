package unit;

import models.sales.*;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.List;

/**
 * 商品Model的单元测试.
 * <p/>
 * User: sujie
 * Date: 2/27/12
 * Time: 5:59 PM
 */
public class GoodsUnitTest extends UnitTest {
    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        Fixtures.delete(Shop.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);
        Fixtures.loadModels("fixture/areas_unit.yml");
        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/brands_unit.yml");
        Fixtures.loadModels("fixture/shops_unit.yml");
        Fixtures.loadModels("fixture/goods_unit.yml");
    }

    @Test
    public void testGetImageBySizeType() {
        models.sales.Goods goods = new Goods();
        goods.imagePath = "/1/1/1/3.jpg";
        String path = goods.getImageLargePath();
        assertEquals("http://img0.uhlcdndev.net/p/1/1/1/3_large.jpg", path);

         path = goods.getImageTinyPath();
        assertEquals("http://img0.uhlcdndev.net/p/1/1/1/3_tiny.jpg", path);

         path = goods.getImageMiddlePath();
        assertEquals("http://img0.uhlcdndev.net/p/1/1/1/3_middle.jpg", path);

         path = goods.getImageSmallPath();
        assertEquals("http://img0.uhlcdndev.net/p/1/1/1/3_small.jpg", path);
    }

    /**
     * 测试各种查询条件都指定的情况.
     */
    @Test
    public void testList() {
        String condition = "0-021-0-0-0-0-0-0-1";
        GoodsCondition goodsCond = new GoodsCondition(condition);

        JPAExtPaginator<Goods> goodsPage = models.sales.Goods.findByCondition
                (goodsCond, 1, 50);
        assertEquals(17, goodsPage.size());
        models.sales.Goods goods = goodsPage.get(0);
        goods.getDiscountExpress();
    }
}
