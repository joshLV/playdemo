package factory.ktv;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.ktv.KtvRoom;
import models.ktv.KtvRoomType;

/**
 * User: tanglq
 * Date: 13-4-11
 * Time: 下午1:42
 */
public class KtvRoomFactory extends ModelFactory<KtvRoom> {

    @Override
    public KtvRoom define() {
        KtvRoom room = new KtvRoom(FactoryBoy.lastOrCreate(KtvRoomType.class));
        room.name = "房间" + FactoryBoy.sequence(KtvRoom.class);
        return room;
    }
}
