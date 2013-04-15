package unit;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;
import factory.resale.ResalerFactory;
import models.accounts.AccountType;
import models.consumer.User;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.ECouponHistoryMessage;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.resale.Resaler;
import models.sales.Goods;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.test.UnitTest;
import util.DateHelper;
import util.mq.MockMQ;

import java.text.ParseException;
import java.util.List;

public class CouponsUnitTest extends UnitTest {
    Supplier supplier;
    User user;
    Goods goods;
    Order order;
    ECoupon ecoupon;
    Resaler yibaiquanResaler;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        MockMQ.clear();

        yibaiquanResaler = ResalerFactory.getYibaiquanResaler();
        supplier = FactoryBoy.create(Supplier.class);
        user = FactoryBoy.create(User.class);
        goods = FactoryBoy.create(Goods.class);
        order = FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order o) {
                o.consumerId = user.id;
                o.userId = yibaiquanResaler.id;
                o.paidAt = DateHelper.beforeDays(1);
                o.createdAt = DateHelper.beforeDays(3);
            }
        });
        ecoupon = FactoryBoy.create(ECoupon.class);
        FactoryBoy.batchCreate(3, ECoupon.class, new SequenceCallback<ECoupon>() {
            @Override
            public void sequence(ECoupon e, int seq) {
                e.order.paidAt = DateHelper.beforeDays(1);
                e.order.save();
            }
        });
    }


    /**
     * 测试券列表
     */
    @Test
    public void testQueryCoupons() {
        List<ECoupon> list = ECoupon.queryCoupons(supplier.id, 1, 15);
        assertEquals(4, list.size());

    }

    /**
     * 测试用户中心券列表
     */
    @Test
    public void testUserQueryCoupons() throws ParseException {
        CouponsCondition condition = new CouponsCondition();
        condition.status = ECouponStatus.UNCONSUMED;
        condition.goodsName = "";
        condition.userId = user.id;
        condition.accountType = AccountType.CONSUMER;
        int pageNumber = 1;
        int pageSize = 15;
        JPAExtPaginator<ECoupon> list = ECoupon.query(condition, pageNumber, pageSize);
        assertEquals(4, list.size());
    }

    /**
     * 测试用户中心券列表
     */
    @Test
    public void testECoupon() {
        final Order order1 = FactoryBoy.create(Order.class);
        List<OrderItems> list = FactoryBoy.batchCreate(3, OrderItems.class,
                new SequenceCallback<OrderItems>() {
                    @Override
                    public void sequence(OrderItems oi, int seq) {
                        oi.goods = FactoryBoy.create(Goods.class);
                        oi.order = order1;
                    }
                });
        order1.orderItems = list;
        order1.save();
        assertEquals(3, list.size());
        ECoupon coupon = null;
        for (OrderItems orderItem : order1.orderItems) {
            coupon = new ECoupon(order1, orderItem.goods, orderItem);
            assertNotNull(coupon);
            assertNotNull(coupon.replyCode);
            assertEquals(4, coupon.replyCode.length());
        }
        assertNotNull(coupon);
    }

    @Test
    public void testFreeze() {
        ECoupon.freeze(ecoupon.id, null);
        ECoupon e = ECoupon.findById(ecoupon.id);
        assertEquals(new Integer(1), e.isFreeze);

        ECouponHistoryMessage lastMessage = (ECouponHistoryMessage) MockMQ.getLastMessage(ECouponHistoryMessage.MQ_KEY);
        assertEquals(ecoupon.id, lastMessage.eCouponId);
        assertEquals("冻结券号", lastMessage.remark);
    }


    @Test
    public void testUnFreeze() {
        ECoupon.unfreeze(ecoupon.id, null);
        ECoupon e = ECoupon.findById(ecoupon.id);
        assertEquals(new Integer(0), e.isFreeze);

        ECouponHistoryMessage lastMessage = (ECouponHistoryMessage) MockMQ.getLastMessage(ECouponHistoryMessage.MQ_KEY);
        assertEquals(ecoupon.id, lastMessage.eCouponId);
        assertEquals("解冻券号", lastMessage.remark);
    }

}
