package functional;

import models.sales.*;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

/**
 * 商品控制器的测试.
 * <p/>
 * User: sujie
 * Date: 2/24/12
 * Time: 5:22 PM
 */
public class GoodsTest extends FunctionalTest {
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
    public void testShow() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        Http.Response response = GET("/goods/" + goodsId);
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(Play.defaultWebEncoding, response);
    }	


    @Test
    public void testList() {
        Http.Response response = GET("/goods/list/0-021-0-0-0-0-0-0-1?page=1");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(Play.defaultWebEncoding, response);
    }

}
