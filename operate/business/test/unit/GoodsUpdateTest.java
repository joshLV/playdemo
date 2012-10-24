package unit;

import factory.FactoryBoy;
import models.sales.Goods;
import models.sales.SecKillGoods;
import models.sales.Shop;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.math.BigDecimal;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-10-23
 * Time: 下午5:48
 * To change this template use File | Settings | File Templates.
 */
public class GoodsUpdateTest extends UnitTest {
    Goods goods;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        goods = FactoryBoy.create(Goods.class);
    }

    @Test
    public void testUpdate() {
        BigDecimal faceValue = new BigDecimal("9.00");
        goods.faceValue = faceValue;
        goods.isAllShop = false;
        goods.shops.clear();
        Shop testShop = FactoryBoy.create(Shop.class, "SupplierId");
        goods.shops.add(testShop);
        goods.save();
        Goods.update(goods.id, goods, false);
        assertEquals(testShop.name, goods.shops.iterator().next().name);
        assertEquals(faceValue, goods.faceValue);
    }
}
