package functional;

import com.google.gson.Gson;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderECouponMessage;
import models.order.OrderItems;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.resale.Resaler;
import models.sales.Goods;
import models.taobao.TaobaoCouponConsumer;
import models.taobao.TaobaoCouponMessage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.test.FunctionalTest;
import util.mq.MockMQ;

import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-1-6
 */
public class TaobaoCouponConsumerTest extends FunctionalTest {
    OuterOrder outerOrder = null;
    Goods goods = null;
    Long taobaoOrderId = 123456L;
    ECoupon ecoupon;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        MockMQ.clear();

        Resaler resaler = FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.loginName = "taobao";
            }
        });

        AccountUtil.getCreditableAccount(resaler.id, AccountType.RESALER);

        goods = FactoryBoy.create(Goods.class);
        ecoupon = FactoryBoy.create(ECoupon.class);

        outerOrder = FactoryBoy.create(OuterOrder.class, new BuildCallback<OuterOrder>() {
            @Override
            public void build(OuterOrder target) {
                Map<String, String> params = new HashMap<>();
                params.put("mobile", "13472581853");
                params.put("num", "2");
                params.put("seller_nick", "券生活8");
                params.put("outer_iid", String.valueOf(goods.id));

                target.ybqOrder = ecoupon.order;
                target.partner = OuterOrderPartner.TB;
                target.status = OuterOrderStatus.ORDER_COPY;
                target.message = new Gson().toJson(params);
                target.orderId = taobaoOrderId;
            }
        });

        FactoryBoy.create(ECoupon.class);
    }

    /**
     * 测试创建订单
     */
    @Ignore
    @Test
    public void testBuildOrder() {
        outerOrder.ybqOrder = null;
        outerOrder.save();

        long orderCount = Order.count();
        long orderItems = OrderItems.count();
        long couponCount = ECoupon.count();


        TaobaoCouponMessage taobaoCouponMessage = new TaobaoCouponMessage(outerOrder.id);

        TaobaoCouponConsumer taobaoCouponConsumer = new TaobaoCouponConsumer();
        taobaoCouponConsumer.consumeWithTx(taobaoCouponMessage);

        assertEquals(orderCount + 1, Order.count());
        assertEquals(orderItems + 1, OrderItems.count());
        assertEquals(couponCount + 2, ECoupon.count());

        outerOrder.refresh();
        assertEquals(OuterOrderStatus.ORDER_SYNCED, outerOrder.status);
    }

    /**
     * 测试重新发送测试
     */
    @Test
    public void testResend() throws Exception {
        outerOrder.status = OuterOrderStatus.RESEND_COPY;
        outerOrder.ybqOrder = ecoupon.order;
        ecoupon.order.refresh();
        outerOrder.save();

        int smsSendCount = ((ECoupon) ECoupon.findAll().get(0)).smsSentCount;

        TaobaoCouponMessage taobaoCouponMessage = new TaobaoCouponMessage(outerOrder.id);

        TaobaoCouponConsumer taobaoCouponConsumer = new TaobaoCouponConsumer();
        taobaoCouponConsumer.consumeWithTx(taobaoCouponMessage);

        OrderECouponMessage msg = (OrderECouponMessage) MockMQ.getLastMessage(OrderECouponMessage.MQ_KEY);
        assertNotNull(msg);

        outerOrder.refresh();
        assertEquals(OuterOrderStatus.RESEND_SYNCED, outerOrder.status);
    }

    @Ignore
    @Test
    public void testSyncOrder() {
        outerOrder.status = OuterOrderStatus.ORDER_DONE;
        outerOrder.save();


        TaobaoCouponMessage taobaoCouponMessage = new TaobaoCouponMessage(outerOrder.id);

        TaobaoCouponConsumer taobaoCouponConsumer = new TaobaoCouponConsumer();
        taobaoCouponConsumer.consumeWithTx(taobaoCouponMessage);

        outerOrder.refresh();
        assertEquals(OuterOrderStatus.ORDER_SYNCED, outerOrder.status);
    }

}
