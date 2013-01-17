package functional;

import factory.FactoryBoy;
import models.sales.PointGoods;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

/**
 * User: hejun
 * Date: 12-8-7
 * Time: 下午1:41
 */
public class PointGoodsFuncTest extends FunctionalTest {

    PointGoods pointGoods;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        pointGoods = FactoryBoy.create(PointGoods.class);
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/index");
        assertIsOk(response);
        assertContentMatch("积分", response);
    }

    @Test
    public void testShow() {
        Http.Response response = GET("/pointgoods/" + pointGoods.id);
        assertStatus(200, response);
        assertContentMatch(pointGoods.name, response);
    }
}
