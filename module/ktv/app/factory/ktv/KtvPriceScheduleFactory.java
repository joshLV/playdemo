package factory.ktv;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvRoomType;
import models.sales.Shop;
import util.DateHelper;

import java.math.BigDecimal;
import java.util.HashSet;

/**
 * User: tanglq
 * Date: 13-4-11
 * Time: 下午1:43
 */
public class KtvPriceScheduleFactory extends ModelFactory<KtvPriceSchedule> {
    @Override
    public KtvPriceSchedule define() {
        KtvPriceSchedule schedule = new KtvPriceSchedule();
        schedule.roomType = FactoryBoy.lastOrCreate(KtvRoomType.class);
        schedule.shops = new HashSet<>();
        schedule.shops.add(FactoryBoy.lastOrCreate(Shop.class));
        schedule.startDay = DateHelper.beforeDays(3);
        schedule.endDay = DateHelper.afterDays(3);
        schedule.startTime  = "01:00";
        schedule.endTime = "24:00";
        schedule.useWeekDay = "1,2,3,4,5";
        schedule.price = BigDecimal.TEN;
        return schedule;
    }
}
