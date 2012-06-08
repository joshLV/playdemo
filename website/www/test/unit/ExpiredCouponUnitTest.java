package unit;

import models.accounts.AccountType;
import models.job.ExpiredCouponNotice;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.order.SentCouponMessage;
import models.sales.*;
import models.sms.MockSMSProvider;
import models.sms.SMSMessage;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * TODO.
 * <p/>
 * User: yanjy
 * Date: 12-5-31
 * Time: 下午3:09
 */
public class ExpiredCouponUnitTest extends UnitTest {
    public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Before
    public void setup() {
        Fixtures.delete(SentCouponMessage.class);
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(Shop.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);
        Fixtures.delete(ECoupon.class);
        Fixtures.loadModels("fixture/areas_unit.yml");
        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/brands_unit.yml");
        Fixtures.loadModels("fixture/shops_unit.yml");
        Fixtures.loadModels("fixture/goods_unit.yml");
        Fixtures.loadModels("fixture/goods.yml");
        Fixtures.loadModels("fixture/orders.yml");
        Fixtures.loadModels("fixture/orderItems.yml");
        Fixtures.loadModels("fixture/sent_coupon_message.yml");
        List<Order> orderList = Order.findAll();
        for (Order order : orderList) {
            order.userType = AccountType.CONSUMER;
            order.save();
        }


    }

    @Test
    public void testJob() throws ParseException {

        List<ECoupon> couponList = ECoupon.findAll();
        for (ECoupon coupon : couponList) {
            if (!"12345670025".equals(coupon.eCouponSn) && !"1234567003".equals(coupon.eCouponSn)) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 7);
                coupon.expireAt = calendar.getTime();
                coupon.save();
            }
        }

        Long id = (Long) Fixtures.idCache.get("models.order.SentCouponMessage-message1");
        List<SentCouponMessage> sentList = SentCouponMessage.findAll();
        assertEquals("1600915935", sentList.get(0).couponNumber);
        assertEquals(1, sentList.size());

        ExpiredCouponNotice job = new ExpiredCouponNotice();
        job.doJob();

        sentList = SentCouponMessage.findAll();
        assertEquals(5, sentList.size());


        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertNotNull("【券市场】您的测试商品5，将要过期，请注意消费截止日期。", msg);
        assertEquals("【券市场】您的测试商品5，将要过期，请注意消费截止日期。", msg.getContent());

        msg = MockSMSProvider.getLastSMSMessage();
        assertNotNull("【券市场】您的测试商品4，将要过期，请注意消费截止日期。", msg);
        assertEquals("【券市场】您的测试商品4，将要过期，请注意消费截止日期。", msg.getContent());


        msg = MockSMSProvider.getLastSMSMessage();
        assertNotNull("【券市场】您的测试商品2，将要过期，请注意消费截止日期。", msg);
        assertEquals("【券市场】您的测试商品2，将要过期，请注意消费截止日期。", msg.getContent());

        msg = MockSMSProvider.getLastSMSMessage();
        assertNotNull("【券市场】您的测试商品1，将要过期，请注意消费截止日期。", msg);
        assertEquals("【券市场】您的测试商品1，将要过期，请注意消费截止日期。", msg.getContent());

        job.doJob();

        sentList = SentCouponMessage.findAll();
        assertEquals(5, sentList.size());

    }
}
