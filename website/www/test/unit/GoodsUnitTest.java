package unit;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.*;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.test.Fixtures;
import play.test.UnitTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

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
        assertEquals("http://img0.uhcdn.com/p/1/1/1/3_large.jpg", path);

        path = goods.getImageTinyPath();
        assertEquals("http://img0.uhcdn.com/p/1/1/1/3_tiny.jpg", path);

        path = goods.getImageMiddlePath();
        assertEquals("http://img0.uhcdn.com/p/1/1/1/3_middle.jpg", path);

        path = goods.getImageSmallPath();
        assertEquals("http://img0.uhcdn.com/p/1/1/1/3_small.jpg", path);
    }


    @Test
    public void testGetDiscountExpress() {
        models.sales.Goods goods = new Goods();
        goods.setDiscount(100);
        assertEquals("", goods.getDiscountExpress());


        goods.setDiscount(10);
        assertEquals("1", goods.getDiscountExpress());


        goods.setDiscount(1);
        assertEquals("0.1", goods.getDiscountExpress());


        goods.setDiscount(12);
        assertEquals("12", goods.getDiscountExpress());


        goods.setDiscount(1000);
        assertEquals("", goods.getDiscountExpress());


        goods.setDiscount(-1);
        assertEquals("", goods.getDiscountExpress());
    }


    @Test
    public void testGetDiscount() {
        models.sales.Goods goods = new Goods();
        assertEquals(new Integer(0), goods.getDiscount());

        goods.originalPrice = new BigDecimal(100);
        goods.salePrice = new BigDecimal(12);
        assertEquals(new Integer(12), goods.getDiscount());

        goods.setDiscount(100);
        assertEquals(new Integer(100), goods.getDiscount());
    }


    /**
     * 测试各种查询条件都指定的情况.
     */
    @Test
    public void testFindByCondition() {
        String condition = "0-021-0-0-0-0-0-0-1";
        GoodsCondition goodsCond = new GoodsCondition(condition);

        JPAExtPaginator<Goods> goodsPage = models.sales.Goods.findByCondition
                (goodsCond, 1, 50);
        assertEquals(17, goodsPage.size());
        models.sales.Goods goods = goodsPage.get(0);
        goods.getDiscountExpress();
    }

    @Test
    public void testUpdateStatus() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        models.sales.Goods goods = Goods.findById(goodsId);
        goods.status = GoodsStatus.ONSALE;
        Goods.updateStatus(GoodsStatus.OFFSALE, goodsId);
        assertEquals(GoodsStatus.OFFSALE, goods.status);
    }

    @Test
    public void testDelete() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        Goods.delete(goodsId);
        models.sales.Goods goods = Goods.findById(goodsId);
        assertEquals(DeletedStatus.DELETED, goods.deleted);

        Long errGoodsId = -1L;
        Goods.delete(errGoodsId);
        goods = Goods.findById(errGoodsId);
        assertNull(goods);
    }

    @Test
    public void testFindTopByCategory() {
        Long categoryId = (Long) Fixtures.idCache.get("models.sales.Category-Category_1");
        List<Goods> goodsList = Goods.findTopByCategory(categoryId, 1);
        assertEquals(1, goodsList.size());
        Set<Category> categories = goodsList.get(0).categories;
        assertEquals(categoryId, categories.iterator().next().id);
    }

    @Test
    public void testFilterShops() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);
        goods.shops = null;
        goods.filterShops();
        assertNotNull(goods.shops);
        assertEquals(2, goods.shops.size());
    }
}
