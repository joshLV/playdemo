package unit;

import models.sales.Goods;
import models.sales.GoodsStatistics;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-4
 * Time: 下午4:58
 */
public class GoodsStatisticsUnitTest extends UnitTest {

    @Before
    public void setup() {
        Fixtures.delete(Goods.class);
        Fixtures.loadModels("fixture/goods.yml");
    }

    @Test
    public void testStatistics() {
        Long id = (Long) Fixtures.idCache.get("models.sales.Goods-goods1");
        Goods goods = Goods.findById(id);
        GoodsStatistics.addCartCount(id);
        GoodsStatistics statistics = GoodsStatistics.find("goodsId", id).first();
        assertEquals(1, statistics.likeCount.intValue());
        assertEquals(1, statistics.cartCount.intValue());

        GoodsStatistics.addCartCount(id);
        statistics = GoodsStatistics.find("goodsId", id).first();
        assertEquals(2, statistics.likeCount.intValue());
        assertEquals(2, statistics.cartCount.intValue());


        GoodsStatistics.addLikeCount(id);
        statistics = GoodsStatistics.find("goodsId", id).first();
        assertEquals(3, statistics.likeCount.intValue());
        assertEquals(2, statistics.cartCount.intValue());

        GoodsStatistics.addBuyCount(id);
        statistics = GoodsStatistics.find("goodsId", id).first();
        assertEquals(1, statistics.buyCount.intValue());

        GoodsStatistics.addVisitorCount(id);
        statistics = GoodsStatistics.find("goodsId", id).first();
        assertEquals(1, statistics.visitorCount.intValue());

        statistics = GoodsStatistics.find("goodsId", id).first();;

        assertEquals(7, statistics.summaryCount.intValue());

    }


}
