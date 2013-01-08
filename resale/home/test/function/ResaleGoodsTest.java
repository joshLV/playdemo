package function;

import controllers.modules.resale.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.resale.Resaler;
import models.sales.Brand;
import models.sales.Goods;
import models.sales.GoodsStatus;
import models.sales.Shop;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import util.DateHelper;

import java.math.BigDecimal;
import java.util.List;

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
        FactoryBoy.create(Brand.class);
        resaler = FactoryBoy.create(Resaler.class);
        shop = FactoryBoy.create(Shop.class);
        FactoryBoy.batchCreate(5, Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods o) {
                o.createdAt = DateHelper.beforeDays(1);
                o.status = GoodsStatus.ONSALE;
            }
        });
        goods = FactoryBoy.create(Goods.class);

        Security.setLoginUserForTest(resaler.loginName);
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/goods");
        assertIsOk(response);
        List<Goods> goodsList = (List) renderArgs("goodsList");
        List<Brand> brandList = (List) renderArgs("brands");
        Resaler resaler1 = (Resaler) renderArgs("resaler");
        assertNotNull(goodsList);
        assertNotNull(resaler1);
        assertNotNull(brandList);
        assertEquals(6, goodsList.size());
        assertEquals(1, brandList.size());
        assertEquals(resaler1, resaler);

    }

    @Test
    public void testShow() {
        Http.Response response = GET("/goods/" + goods.id);
        assertIsOk(response);

        assertNotNull(renderArgs("goods"));
    }

    @Test
    public void testList_condition() {
        goods.resaleAddPrice = new BigDecimal("50");
        goods.save();
        Http.Response response = GET("/goods/list/0-30-60-9-0?page=1");
        assertIsOk(response);
        List<Goods> goodsList = (List) renderArgs("goodsList");
        assertNotNull(goodsList);
        assertEquals(1, goodsList.size());
        Resaler resaler1 = (Resaler) renderArgs("resaler");
        assertEquals(resaler1, resaler);
    }

    @Test
    public void testList() {
        goods.isLottery = true;
        goods.save();
        Http.Response response = GET("/goods/list/0-0-0-0-0?page=1");
        assertIsOk(response);
        List<Goods> goodsList = (List) renderArgs("goodsList");
        assertNotNull(goodsList);
        assertEquals(5, goodsList.size());
        Resaler resaler1 = (Resaler) renderArgs("resaler");
        assertEquals(resaler1, resaler);
    }
}
