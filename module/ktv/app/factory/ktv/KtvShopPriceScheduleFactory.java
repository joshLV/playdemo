package factory.ktv;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvShopPriceSchedule;
import models.sales.Shop;

/**
 * User: yan
 * Date: 13-5-9
 * Time: 下午3:11
 */
public class KtvShopPriceScheduleFactory extends ModelFactory<KtvShopPriceSchedule> {
    @Override
    public KtvShopPriceSchedule define() {
        KtvShopPriceSchedule shopPriceSchedule = new KtvShopPriceSchedule();
        shopPriceSchedule.shop = FactoryBoy.lastOrCreate(Shop.class);
        shopPriceSchedule.roomCount = 100;
        shopPriceSchedule.schedule = FactoryBoy.lastOrCreate(KtvPriceSchedule.class);
        return shopPriceSchedule;
    }
}
