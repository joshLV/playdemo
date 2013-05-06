package unit;

import factory.FactoryBoy;
import models.job.resale.KtvOrderCancel;
import models.ktv.KtvOrderStatus;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvRoomOrderInfo;
import models.order.OrderItems;
import models.order.OrderStatus;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.DateHelper;

/**
 * User: yan
 * Date: 13-4-16
 * Time: 下午4:24
 */
public class KtvOrderCancelUnitTest extends UnitTest {
    OrderItems orderItems;

    KtvRoomOrderInfo orderInfo1;
    KtvRoomOrderInfo orderInfo;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        orderItems = FactoryBoy.create(OrderItems.class);
//        KtvRoom ktvRoomA = FactoryBoy.create(KtvRoom.class);

        KtvPriceSchedule schedule = FactoryBoy.create(KtvPriceSchedule.class);
//        schedule.roomType = ktvRoomA.roomType;
        schedule.save();

        orderInfo1 = FactoryBoy.create(KtvRoomOrderInfo.class);
        orderInfo = FactoryBoy.create(KtvRoomOrderInfo.class);
        orderInfo.orderItem = orderItems;
        orderInfo.status = KtvOrderStatus.LOCK;
        orderInfo.createdAt = DateHelper.beforeMinuts(11);
        orderInfo.save();
    }

    @Test
    public void testJob() {
        assertEquals(KtvOrderStatus.LOCK, orderInfo.status);
        assertEquals(OrderStatus.UNPAID, orderInfo.orderItem.status);
        assertEquals(OrderStatus.UNPAID, orderInfo1.orderItem.status);
        assertEquals(KtvOrderStatus.LOCK, orderInfo1.status);
        KtvOrderCancel orderCancel = new KtvOrderCancel();
        orderCancel.doJob();
        orderInfo.refresh();

        assertEquals(OrderStatus.UNPAID, orderInfo1.orderItem.status);
        assertEquals(KtvOrderStatus.LOCK, orderInfo1.status);
        assertEquals(KtvOrderStatus.CANCELED, orderInfo.status);
        assertEquals(OrderStatus.CANCELED, orderInfo.orderItem.status);
    }
}
