package factory.ktv;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.ktv.KtvPriceSchedue;
import models.ktv.KtvRoomType;
import models.sales.Shop;

import java.util.HashSet;

/**
 * User: tanglq
 * Date: 13-4-11
 * Time: 下午1:43
 */
public class KtvPriceSchedueFactory extends ModelFactory<KtvPriceSchedue> {
    @Override
    public KtvPriceSchedue define() {
        KtvPriceSchedue schedue = new KtvPriceSchedue();
        schedue.roomType = FactoryBoy.lastOrCreate(KtvRoomType.class);
        schedue.shops = new HashSet<>();
        schedue.shops.add(FactoryBoy.lastOrCreate(Shop.class));
        return schedue;
    }
}
