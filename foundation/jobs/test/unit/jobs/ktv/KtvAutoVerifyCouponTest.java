package unit.jobs.ktv;

import com.uhuila.common.util.DateUtil;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;
import jobs.ktv.KtvAutoVerifyCoupon;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.admin.SupplierUser;
import models.ktv.KtvOrderStatus;
import models.ktv.KtvProduct;
import models.ktv.KtvRoomOrderInfo;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.OrderItems;
import models.supplier.Supplier;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: yan
 * Date: 13-5-29
 * Time: 下午4:26
 */
public class KtvAutoVerifyCouponTest extends UnitTest {
    OrderItems orderItem;
    List<ECoupon> couponList;
    KtvRoomOrderInfo roomOrderInfo;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        orderItem = FactoryBoy.create(OrderItems.class);
        FactoryBoy.lastOrCreate(SupplierUser.class);
        roomOrderInfo = FactoryBoy.create(KtvRoomOrderInfo.class, new BuildCallback<KtvRoomOrderInfo>() {
            @Override
            public void build(KtvRoomOrderInfo target) {
                target.scheduledTime = 13;
                target.product = FactoryBoy.lastOrCreate(KtvProduct.class, "twoHours");
                target.status = KtvOrderStatus.DEAL;
                target.orderItem = orderItem;
            }
        });
        Account account = AccountUtil.getPlatformIncomingAccount(); //默认收款账户为平台收款账户
        account.amount = new BigDecimal("999999");
        account.save();
    }

    @Test
    public void test不满足条件的_明天的预订() {
        roomOrderInfo.scheduledDay = DateUtils.addDays(new Date(), 1);
        roomOrderInfo.save();
        createWithPasswordCoupons(1);
        assertEquals(ECouponStatus.UNCONSUMED, couponList.get(0).status);
        assertNull(couponList.get(0).consumedAt);
        KtvAutoVerifyCoupon job = new KtvAutoVerifyCoupon();
        job.doJobWithHistory();

        couponList.get(0).refresh();
        assertEquals(ECouponStatus.UNCONSUMED, couponList.get(0).status);
        assertNull(couponList.get(0).consumedAt);
    }

    @Test
    public void test不满足条件的_当天的预订还没到时间的() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        roomOrderInfo.scheduledTime = hour + 1;
        roomOrderInfo.save();
        createWithPasswordCoupons(1);
        assertEquals(ECouponStatus.UNCONSUMED, couponList.get(0).status);
        assertNull(couponList.get(0).consumedAt);
        KtvAutoVerifyCoupon job = new KtvAutoVerifyCoupon();
        job.doJobWithHistory();

        couponList.get(0).refresh();
        assertEquals(ECouponStatus.UNCONSUMED, couponList.get(0).status);
        assertNull(couponList.get(0).consumedAt);
    }

    @Test
    public void test满足条件的_当天的预订已经过1小时的() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        roomOrderInfo.scheduledTime = hour - 2;
        roomOrderInfo.save();
        createWithPasswordCoupons(1);
        assertEquals(ECouponStatus.UNCONSUMED, couponList.get(0).status);
        assertNull(couponList.get(0).consumedAt);
        KtvAutoVerifyCoupon job = new KtvAutoVerifyCoupon();
        job.doJobWithHistory();

        couponList.get(0).refresh();
        assertEquals(ECouponStatus.CONSUMED, couponList.get(0).status);
        assertNotNull(couponList.get(0).consumedAt);
    }

    @Test
    public void test满足条件的() throws Exception {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        roomOrderInfo.scheduledTime = hour-2;
        roomOrderInfo.save();
        createWithPasswordCoupons(1);
        assertEquals(ECouponStatus.UNCONSUMED, couponList.get(0).status);
        assertNull(couponList.get(0).consumedAt);
        KtvAutoVerifyCoupon job = new KtvAutoVerifyCoupon();
        job.doJobWithHistory();
        Thread.sleep(500l);
        couponList.get(0).refresh();
        assertEquals(ECouponStatus.CONSUMED, couponList.get(0).status);
        assertNotNull(couponList.get(0).consumedAt);
    }

    private void createWithPasswordCoupons(int size) {
        couponList = FactoryBoy.batchCreate(size, ECoupon.class, "password",
                new SequenceCallback<ECoupon>() {
                    @Override
                    public void sequence(ECoupon target, int seq) {
                        target.status = ECouponStatus.UNCONSUMED;
                        target.isFreeze = 0;
                        target.createdAt = new Date();
                        target.orderItems = orderItem;
                        target.expireAt = DateUtil.getEndOfDay(roomOrderInfo.scheduledDay);
                    }
                });
    }
}
