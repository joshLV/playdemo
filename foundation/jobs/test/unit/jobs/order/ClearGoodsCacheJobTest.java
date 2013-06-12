package unit.jobs.order;

import com.uhuila.common.util.DateUtil;
import factory.FactoryBoy;
import jobs.order.ClearGoodsCacheJob;
import models.jobs.JobWithHistory;
import models.sales.Goods;
import models.sales.GoodsSchedule;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.DateHelper;

import java.util.Date;
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
        JobWithHistory.cleanLastBeginRunAtForTest();
    }

    @Test
    public void testJob() throws Exception {
        Goods goods = FactoryBoy.create(Goods.class);
        goods.effectiveAt = DateHelper.beforeDays(2);
        goods.beginOnSaleAt = DateHelper.afterMinuts(10);
        goods.save();


        List<Goods> goodsList = Goods.findNewGoods(5);
        int n = goodsList.size();
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

        //测试昨天有前排期的商品，但是今天不会显示
        GoodsSchedule goodsSchedule = FactoryBoy.create(GoodsSchedule.class);
        goodsSchedule.goods = goods;
        goodsSchedule.effectiveAt = DateHelper.beforeDays(1);
        goodsSchedule.save();

        job.doJob();
        Date currDate = new Date();
        List<GoodsSchedule> goodsScheduleList = GoodsSchedule.findSchedule(goods, currDate);
        assertEquals(0, goodsScheduleList.size());

        //测试今天有前排期的商品 , 今天不会显示
        goodsSchedule.refresh();
        goodsSchedule.goods = goods;
        goodsSchedule.effectiveAt = currDate;
        goodsSchedule.expireAt = DateUtil.getEndOfDay();
        goodsSchedule.save();
        job.doJob();
        goodsScheduleList = GoodsSchedule.findSchedule(goods, currDate);
        assertEquals(1, goodsScheduleList.size());

        //测试明天有前排期的商品 , 但今天不会显示
        goodsSchedule.refresh();
        goodsSchedule.effectiveAt = DateHelper.afterDays(1);
        goodsSchedule.save();
        job.doJob();
        goodsScheduleList = GoodsSchedule.findSchedule(goods, currDate);
        assertEquals(0, goodsScheduleList.size());
    }
}
