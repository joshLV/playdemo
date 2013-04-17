package factory.ktv;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.ktv.KtvOrderStatus;
import models.ktv.KtvRoom;
import models.ktv.KtvRoomOrderInfo;
import models.ktv.KtvRoomType;
import models.order.OrderItems;
import models.sales.Goods;
import models.sales.Shop;

import java.util.Date;

/**
 * User: yan
 * Date: 13-4-16
 * Time: 下午4:41
 */
public class KtvRoomOrderInfoFactory extends ModelFactory<KtvRoomOrderInfo> {

    @Override
    public KtvRoomOrderInfo define() {
        KtvRoom ktvRoom = FactoryBoy.lastOrCreate(KtvRoom.class);
        KtvRoomOrderInfo roomOrderInfo = new KtvRoomOrderInfo(FactoryBoy.lastOrCreate(Goods.class),
                FactoryBoy.create(OrderItems.class), ktvRoom,
                ktvRoom.roomType, new Date(), "09:00");
        roomOrderInfo.status = KtvOrderStatus.LOCK;
        roomOrderInfo.createdAt = new Date();
        return roomOrderInfo;
    }
}
