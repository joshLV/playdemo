package function;

import models.resale.Resaler;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.sales.GoodsLevelPrice;
import models.sales.Shop;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

/**
 * 分销商品控制器的测试.
 * <p/>
 * User: yanjy
 * Date: 3/26/12
 * Time: 5:22 PM
 */
public class GoodsTest extends FunctionalTest {
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
    public void testShow() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        Http.Response response = GET("/goods/" + goodsId);
        assertStatus(302,response);
    }	


    @Test
    public void testList() {
        Http.Response response = GET("/goods/list/0-0-0-0-0?page=1");
        assertStatus(302,response);
    }

}
