package unit;

import java.math.BigDecimal;

import models.resale.Resaler;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.sales.GoodsCondition;
import models.sales.GoodsLevelPrice;
import models.sales.Shop;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.modules.paginate.JPAExtPaginator;
import play.test.Fixtures;
import play.test.UnitTest;

/**
 * 商品Model的单元测试.
 * <p/>
 * User: yanjy
 * Date: 3/26/12
 * Time: 5:59 PM
 */
public class GoodsUnitTest extends UnitTest {
    @Before
    public void setup() {
        Fixtures.delete(Shop.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);
        Fixtures.delete(Resaler.class);
        Fixtures.delete(GoodsLevelPrice.class);
        Fixtures.loadModels("fixture/areas_unit.yml");
        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/brands_unit.yml");
        Fixtures.loadModels("fixture/shops_unit.yml");
        Fixtures.loadModels("fixture/goods_unit.yml");
        Fixtures.loadModels("fixture/level_price.yml");
        Fixtures.loadModels("fixture/resaler.yml");
    }

    @Test
    public void testGetResalePrice() {
        models.sales.Goods goods = new Goods();
        assertEquals(new Integer(0), goods.getDiscount());

        goods.faceValue = new BigDecimal(100);
        goods.salePrice = new BigDecimal(12);
        assertEquals(new Integer(12), goods.getDiscount());

        goods.setDiscount(100f);
        assertEquals(new Integer(100), goods.getDiscount());
    }


    /**
     * 测试各种查询条件都指定的情况.
     */
    @Test
    public void testFindByResaleCondition() {
        String condition = "0-0-0-0-1";
        GoodsCondition goodsCond = new GoodsCondition(true,condition);
        Long resalerId = (Long) Fixtures.idCache.get("models.resale.Resaler-Resaler_1");
        Resaler resaler =Resaler.findById(resalerId);
        JPAExtPaginator<Goods> goodsPage = models.sales.Goods.findByResaleCondition
                (resaler,goodsCond, 1, 50);
        assertEquals(15, goodsPage.size());
    }
}
