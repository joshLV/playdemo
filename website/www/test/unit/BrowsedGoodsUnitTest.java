package unit;

import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.sales.BrowsedGoods;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.util.List;

/**
 * 浏览过的商品的测试.
 * <p/>
 * User: sujie
 * Date: 11/9/12
 * Time: 4:07 PM
 */
public class BrowsedGoodsUnitTest extends UnitTest {

    BrowsedGoods browsedGoods;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        FactoryBoy.deleteAll();

        browsedGoods = FactoryBoy.create(BrowsedGoods.class);
    }

    @Test
    public void testFindTop() {
        List<BrowsedGoods> goodsList = FactoryBoy.batchCreate(10, BrowsedGoods.class,
                new SequenceCallback<BrowsedGoods>() {
                    @Override
                    public void sequence(BrowsedGoods target, int seq) {
                        target.goods = browsedGoods.goods;
                        target.visitorCount = 2;
                    }
                });
        for (BrowsedGoods goods : goodsList) {
            System.out.println("goods.visitorCount:" + goods.visitorCount);
        }
        List<BrowsedGoods> browsedGoodsList = BrowsedGoods.findTop(4, 2);
        assertEquals(1, browsedGoodsList.size());

        BrowsedGoods distinctBrowsedGoods = browsedGoodsList.get(0);
        assertEquals(20 + browsedGoods.visitorCount, distinctBrowsedGoods.visitorCount.intValue());
    }

}
