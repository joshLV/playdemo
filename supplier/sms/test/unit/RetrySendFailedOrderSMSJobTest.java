package unit;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.job.RetrySendFailedOrderSMSJob;
import models.order.ECoupon;
import models.order.OrderECouponMessage;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.test.UnitTest;
import util.DateHelper;
import util.mq.MockMQ;

/**
 * User: tanglq
 * Date: 13-1-31
 * Time: 下午2:05
 */
public class RetrySendFailedOrderSMSJobTest extends UnitTest {
    @Before
    public void setUp() throws Exception {
        FactoryBoy.deleteAll();
        MockMQ.clear();
    }

    @Test
    public void testSelectOne() throws Exception {

        ECoupon ecoupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.createdAt = DateHelper.beforeMinuts(60);
            }
        });

        new RetrySendFailedOrderSMSJob().doJob();

        OrderECouponMessage lastMsg = (OrderECouponMessage) MockMQ.getLastMessage(OrderECouponMessage.MQ_KEY);
        Logger.info("lastMsg=" + lastMsg);
        assertEquals(ecoupon.orderItems.id, lastMsg.orderItemId);
    }


    @Test
    public void testSelectNoneIn3Min() throws Exception {

        ECoupon ecoupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.createdAt = DateHelper.beforeMinuts(3);
            }
        });

        new RetrySendFailedOrderSMSJob().doJob();

        assertEquals(0, MockMQ.size(OrderECouponMessage.MQ_KEY));
    }


    @Test
    public void testSelectNoneIn3HoursBefore() throws Exception {

        ECoupon ecoupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.createdAt = DateHelper.beforeHours(3);
            }
        });

        new RetrySendFailedOrderSMSJob().doJob();

        assertEquals(0, MockMQ.size(OrderECouponMessage.MQ_KEY));
    }
}
