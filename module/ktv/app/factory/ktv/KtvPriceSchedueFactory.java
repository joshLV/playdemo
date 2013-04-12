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
public class KtvPriceSchedueFactory extends ModelFactory<KtvPriceSchedule> {
    @Override
    public KtvPriceSchedule define() {
        KtvPriceSchedule schedue = new KtvPriceSchedule();
        schedue.roomType = FactoryBoy.lastOrCreate(KtvRoomType.class);
        schedue.shops = new HashSet<>();
        schedue.shops.add(FactoryBoy.lastOrCreate(Shop.class));
        schedue.startDay = DateHelper.beforeDays(3);
        schedue.endDay = DateHelper.afterDays(3);
        schedue.startTime  = "01:00";
        schedue.endTime = "24:00";
        schedue.useWeekDay = "1,2,3,4,5";
        schedue.price = BigDecimal.TEN;
        return schedue;
    }
}
