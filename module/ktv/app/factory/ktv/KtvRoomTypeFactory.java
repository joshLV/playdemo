package factory.ktv;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.ktv.KtvRoomType;
import models.supplier.Supplier;

/**
 * User: tanglq
 * Date: 13-4-11
 * Time: 下午1:39
 */
public class KtvRoomTypeFactory extends ModelFactory<KtvRoomType> {

    @Override
    public KtvRoomType define() {
        KtvRoomType ktvRoomType = new KtvRoomType();
        ktvRoomType.name = "房型" + FactoryBoy.sequence(KtvRoomType.class);
        ktvRoomType.supplier = FactoryBoy.lastOrCreate(Supplier.class);
        return ktvRoomType;
    }
}
