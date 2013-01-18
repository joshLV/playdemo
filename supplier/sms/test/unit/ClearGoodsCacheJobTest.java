package unit;

import factory.FactoryBoy;
import models.job.ClearGoodsCacheJob;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.DateHelper;

import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-11-19
 * Time: 下午3:31
 */
public class ClearGoodsCacheJobTest extends UnitTest {
    @Before
    public void setup() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testJob() {
        Goods goods = FactoryBoy.create(Goods.class);
        goods.effectiveAt = DateHelper.beforeDays(2);
        goods.beginOnSaleAt = DateHelper.afterMinuts(10);
        goods.save();



        List<Goods> goodsList = Goods.findNewGoods(5);
        int n = goodsList.size();
        System.out.println(goodsList.size() + ">>>>goodsList.size()");
        assertEquals(0, n);

        goods.beginOnSaleAt = DateHelper.beforeMinuts(1);
//        goods.expireAt = DateHelper.afterHours(2);
        goods.endOnSaleAt = DateHelper.afterHours(2);
        goods.save();
        ClearGoodsCacheJob job = new ClearGoodsCacheJob();
        job.doJob();
        goodsList = Goods.findNewGoods(5);
        assertEquals(n + 1, goodsList.size());

//        goods.expireAt = DateHelper.beforeMinuts(1);
        goods.endOnSaleAt = DateHelper.beforeMinuts(1);
        goods.save();
        job.doJob();
        goodsList = Goods.findNewGoods(5);
        assertEquals(0, goodsList.size());

//        GoodsSchedule goodsSchedule = FactoryBoy.create(GoodsSchedule.class);
//        goodsSchedule.goods = goods;
//        goodsSchedule.effectiveAt = new Date();
//        goodsSchedule.save();

    }
}
