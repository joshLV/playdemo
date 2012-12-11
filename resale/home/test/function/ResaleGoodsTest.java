package function;

import controllers.modules.resale.cas.Security;
import factory.FactoryBoy;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.Shop;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

/**
 * 分销商品控制器的测试.
 * <p/>
 * User: yanjy
 * Date: 3/26/12
 * Time: 5:22 PM
 */
public class ResaleGoodsTest extends FunctionalTest {
    Shop shop;
    Goods goods;
    Resaler resaler;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        resaler = FactoryBoy.create(Resaler.class);
        shop = FactoryBoy.create(Shop.class);
        goods = FactoryBoy.create(Goods.class);

        Security.setLoginUserForTest(resaler.loginName);
    }

    @Test
    public void testShow() {
        Http.Response response = GET("/goods/" + goods.id);
        assertIsOk(response);

        assertNotNull(renderArgs("goods"));
    }


    @Test
    public void testList() {
        Http.Response response = GET("/goods/list/0-0-0-0-0?page=1");
        assertIsOk(response);
    }

}
