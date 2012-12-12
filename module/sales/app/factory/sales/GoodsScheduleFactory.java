package factory.sales;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.sales.Goods;
import models.sales.GoodsSchedule;

import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-11
 * Time: 下午4:11
 */
public class GoodsScheduleFactory extends ModelFactory<GoodsSchedule> {
    @Override
    public GoodsSchedule define() {
        Goods goods = FactoryBoy.create(Goods.class);
        GoodsSchedule goodsSchedule = new GoodsSchedule();
        goodsSchedule.goods = goods;
        goodsSchedule.effectiveAt = new Date();
        goodsSchedule.expireAt = new Date();
        goodsSchedule.createdAt = new Date();
        return goodsSchedule;
    }
}
