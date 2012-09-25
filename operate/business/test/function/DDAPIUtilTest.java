package function;

import factory.FactoryBoy;
import models.dangdang.DDAPIInvokeException;
import models.dangdang.DDAPIUtil;
import models.order.ECoupon;
import models.sales.Goods;
import org.junit.Test;
import play.mvc.Before;
import play.test.FunctionalTest;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 9/25/12
 * Time: 2:58 PM
 */
public class DDAPIUtilTest extends FunctionalTest {
    @Before
    public void setup() {
        FactoryBoy.lazyDelete();
    }

    @Test
    public void tesSyncSellCount() {
        Goods goods = FactoryBoy.create(Goods.class);

        try {
            DDAPIUtil.syncSellCount(goods);
        } catch (DDAPIInvokeException e) {
            fail();
        }
    }

    @Test
    public void testIsRefund() {
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class);
        try {
            boolean isRefund = DDAPIUtil.isRefund(ecoupon);
            assertFalse(isRefund);
        } catch (DDAPIInvokeException e) {
            fail();
        }
    }

    @Test
    public void testNotifyVerified() {
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class);
        try {
            DDAPIUtil.notifyVerified(ecoupon);
        } catch (DDAPIInvokeException e) {
            fail();
        }
    }

}
