package functional;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import models.order.*;
import models.resale.Resaler;
import models.sina.SinaVouchersConsumer;
import org.junit.Before;
import org.junit.Test;
import play.test.FunctionalTest;
import util.ws.MockWebServiceClient;

/**
 * User: yan
 * Date: 13-3-28
 * Time: 上午10:06
 */
public class SinaVouchersConsumerTest extends FunctionalTest {
    ECoupon coupon;
    OrderItems orderItems;
    Order order;
    User user;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        Resaler resaler = FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.loginName = Resaler.SINA_LOGIN_NAME;
            }
        });
        user = FactoryBoy.create(User.class);
        user.openId = "214waiqrjuqwior";
        user.save();
        order = FactoryBoy.create(Order.class);
        order.userId = resaler.id;
        order.consumerId = user.id;
        order.userType = AccountType.RESALER;
        order.save();

        orderItems = FactoryBoy.create(OrderItems.class);
        orderItems.outerGoodsNo = "123";
        orderItems.order = order;
        orderItems.save();

        coupon = FactoryBoy.create(ECoupon.class);
        coupon.partner = ECouponPartner.SINA;
        coupon.synced = false;
        coupon.order = order;
        coupon.orderItems = orderItems;
        coupon.save();

        AccountUtil.getCreditableAccount(resaler.id, AccountType.RESALER);
        MockWebServiceClient.clear();
    }

    @Test
    public void testConsumer() {
        String data = "{\"header\":{\"signature\":\"615046389fd8c0022e641fb2e5e142da\",\"member_id\":\"123\",\"sequence\n" +
                "\":\"1234\"},\"content\":\"{\\\"id\\\":\\\"456\\\"}\"}";
        MockWebServiceClient.addMockHttpRequest(200, data);
        SinaVouchersConsumer consumer = new SinaVouchersConsumer();
        consumer.consumeWithTx(coupon.id);

        coupon.refresh();
        assertEquals("456", coupon.partnerCouponId);
        assertTrue(coupon.synced);
    }
}
