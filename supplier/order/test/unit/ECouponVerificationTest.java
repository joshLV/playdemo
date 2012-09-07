package unit;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.order.ECoupon;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.DateHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ECouponVerificationTest extends UnitTest {

    @Before
    public void setUp() {
        FactoryBoy.lazyDelete();
    }

    @Test
    public void 在验证时间段内可验证() throws Exception {
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                Date date1 = DateHelper.beforeMinuts(new Date(), 1);
                Date date2 = DateHelper.afterMinuts(new Date(), 1);
                SimpleDateFormat sdf = new SimpleDateFormat(ECoupon.TIME_FORMAT);
                target.goods.useBeginTime = sdf.format(date1);
                target.goods.useEndTime = sdf.format(date2);
            }
        });
        assertTrue(ecoupon.checkVerifyTimeRegion(new Date()));
    }

    @Test
    public void 不在验证时间段内不可验证() throws Exception {
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                Date date1 = DateHelper.beforeMinuts(new Date(), 2);
                Date date2 = DateHelper.beforeMinuts(new Date(), 1);
                SimpleDateFormat sdf = new SimpleDateFormat(ECoupon.TIME_FORMAT);
                target.goods.useBeginTime = sdf.format(date1);
                target.goods.useEndTime = sdf.format(date2);
            }
        });
        assertFalse(ecoupon.checkVerifyTimeRegion(new Date()));
    }
}
