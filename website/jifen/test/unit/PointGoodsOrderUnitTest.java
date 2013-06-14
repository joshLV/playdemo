package unit;

import factory.FactoryBoy;
import models.consumer.Address;
import models.order.PointGoodsOrder;
import models.order.PointGoodsOrderSentStatus;
import models.order.PointGoodsOrderStatus;
import models.sales.PointGoods;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

/**
 * 积分订单的单元测试.
 * <p/>
 * User: hejun
 * Date: 12-8-9
 * Time: 下午1:16
 */
public class PointGoodsOrderUnitTest extends UnitTest {

    PointGoodsOrder pointGoodsOrder;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        pointGoodsOrder = FactoryBoy.create(PointGoodsOrder.class);
    }

    @Test
    public void testOrderInit() {
        Long buyNumber = 2L;
        Long userId = pointGoodsOrder == null ? 0 : pointGoodsOrder.userId;
        PointGoods pointGoods = pointGoodsOrder == null ? null : pointGoodsOrder.pointGoods;

        PointGoodsOrder pointGoodsOrder = new PointGoodsOrder(userId, pointGoods, buyNumber);
        assertEquals("2", pointGoodsOrder.buyNumber.toString());
        assertEquals(pointGoodsOrder.pointGoods.name, pointGoodsOrder.pointGoodsName);

    }

    @Test
    public void testSetAddress() {
        Address address = FactoryBoy.create(Address.class);
        pointGoodsOrder.setAddress(address);

        assertEquals(address.mobile, pointGoodsOrder.receiverMobile);
        assertEquals(address.name, pointGoodsOrder.receiverName);
        assertEquals(address.postcode, pointGoodsOrder.postcode);
    }

    @Test
    public void testCheckLimitNumber() throws Exception {
        boolean b = PointGoodsOrder.checkLimitNumber(pointGoodsOrder.pointGoods.id, pointGoodsOrder.buyNumber);
        assertFalse(b);
    }

    @Test
    public void testCreateAndCancel() {
        pointGoodsOrder.createAndUpdateInventory();
        pointGoodsOrder.refresh();
        assertEquals(7, pointGoodsOrder.pointGoods.baseSale.intValue());
        assertEquals(0, pointGoodsOrder.totalPoint);


        pointGoodsOrder.cancelAndUpdateOrder();
        assertEquals(8, pointGoodsOrder.pointGoods.baseSale.intValue());
        assertEquals(0, pointGoodsOrder.totalPoint);
        assertEquals(PointGoodsOrderStatus.CANCELED, pointGoodsOrder.status);
    }

    @Test
    public void testAccept() {
        pointGoodsOrder.status = PointGoodsOrderStatus.APPLY;
        pointGoodsOrder.accept(11l);
        pointGoodsOrder.save();

        assertEquals(PointGoodsOrderStatus.ACCEPT, pointGoodsOrder.status);

    }

    @Test
    public void testAcceptOrder() {
        pointGoodsOrder.status = PointGoodsOrderStatus.APPLY;
        pointGoodsOrder.save();

        PointGoodsOrder.acceptOrder(pointGoodsOrder.id);
        assertEquals(PointGoodsOrderStatus.ACCEPT, pointGoodsOrder.status);
    }

    @Test
    public void testCancelOrder() {
        pointGoodsOrder.status = PointGoodsOrderStatus.APPLY;
        pointGoodsOrder.save();

        PointGoodsOrder.cancelOrder(pointGoodsOrder.id, "no");
        assertEquals(PointGoodsOrderStatus.CANCELED, pointGoodsOrder.status);

    }

    @Test
    public void testSendGoods() {
        pointGoodsOrder.status = PointGoodsOrderStatus.ACCEPT;
        pointGoodsOrder.save();
        pointGoodsOrder.refresh();
        PointGoodsOrder.sendGoods(pointGoodsOrder.id, " ");
        assertEquals(PointGoodsOrderSentStatus.SENT, pointGoodsOrder.sentStatus);

    }

    @Test
    public void testFindByOrderNumber() {
        PointGoodsOrder foundOrder = PointGoodsOrder.findByOrderNumber(pointGoodsOrder.orderNumber);
        assertNotNull(foundOrder);
    }

    @Test
    public void testGetUser() {
        assertNotNull(pointGoodsOrder.getUser());

    }

    @Test
    public void testContiansRealGoods() {
        assertFalse(pointGoodsOrder.containsRealGoods());
    }
}
