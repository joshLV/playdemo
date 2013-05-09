package factory.ktv;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvProduct;
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
        schedule.roomType = KtvRoomType.MIDDLE;
        schedule.startDay = DateHelper.beforeDays(3);
        schedule.endDay = DateHelper.afterDays(3);
        schedule.dayOfWeeks = "1,2,3,4,5,6,7";
        schedule.price = BigDecimal.TEN;
        schedule.deleted = DeletedStatus.UN_DELETED;
        schedule.product = FactoryBoy.lastOrCreate(KtvProduct.class);
        schedule.startTimes = "8,15";
        return schedule;
    }
}
