package unit;

import models.sales.Goods;
import models.sales.GoodsStatistics;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import factory.FactoryBoy;
import factory.callback.SequenceCallback;

import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-4
 * Time: 下午4:58
 */
@Ignore
public class GoodsStatisticsUnitTest extends UnitTest {
    List<Goods> goodsList;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        goodsList = FactoryBoy.batchCreate(2, Goods.class,
                new SequenceCallback<Goods>() {
                    @Override
                    public void sequence(Goods target, int seq) {
                        target.name = "Test#" + seq;
                    }
                });
    }

    @Test
    public void testStatistics() {
        GoodsStatistics.addCartCount(goodsList.get(0).id);
        GoodsStatistics statistics = GoodsStatistics.find("goodsId", goodsList.get(0).id).first();
        assertEquals(1, statistics.likeCount.intValue());
        assertEquals(1, statistics.cartCount.intValue());

        GoodsStatistics.addCartCount(goodsList.get(0).id);
        statistics = GoodsStatistics.find("goodsId", goodsList.get(0).id).first();
        assertEquals(2, statistics.likeCount.intValue());
        assertEquals(2, statistics.cartCount.intValue());


        GoodsStatistics.addLikeCount(goodsList.get(0).id);
        statistics = GoodsStatistics.find("goodsId", goodsList.get(0).id).first();
        assertEquals(3, statistics.likeCount.intValue());
        assertEquals(2, statistics.cartCount.intValue());

        GoodsStatistics.addBuyCount(goodsList.get(0).id);
        statistics = GoodsStatistics.find("goodsId", goodsList.get(0).id).first();
        assertEquals(1, statistics.buyCount.intValue());

        GoodsStatistics.addVisitorCount(goodsList.get(0).id);
        statistics = GoodsStatistics.find("goodsId", goodsList.get(0).id).first();
        assertEquals(1, statistics.visitorCount.intValue());

        statistics = GoodsStatistics.find("goodsId", goodsList.get(0).id).first();
        ;

        assertEquals(37, statistics.summaryCount.intValue());

    }


}
