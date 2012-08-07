package functional;

import models.sales.*;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-7
 * Time: 下午1:41
 * To change this template use File | Settings | File Templates.
 */
public class PointGoodsFuncTest extends FunctionalTest {

    @Before
    public void setUp(){
        Fixtures.delete(PointGoods.class);


        Fixtures.delete(Shop.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);
        Fixtures.loadModels("Fixture/pointgoods.yml");
        Fixtures.loadModels("Fixture/areas_unit.yml");
        Fixtures.loadModels("Fixture/categories_unit.yml");
        Fixtures.loadModels("Fixture/supplier_unit.yml");
        Fixtures.loadModels("Fixture/brands_unit.yml");
        Fixtures.loadModels("Fixture/shops_unit.yml");
        Fixtures.loadModels("Fixture/goods_unit.yml");
    }

    @Test
    public void testIndex(){
        Http.Response response = GET("/index");
        assertIsOk(response);
        assertContentMatch("积分", response);
    }

    @Test
    public void testShow(){
        Long id = (Long) Fixtures.idCache.get("models.sales.PointGoods-pointgoods1");
        assertNotNull(id);

        Http.Response response = GET("/pointgoods/" + id);
        assertStatus(200, response);
        assertContentMatch("cat88",response);
    }
}
