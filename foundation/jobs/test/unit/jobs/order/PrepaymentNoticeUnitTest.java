package unit.jobs.order;

import com.uhuila.common.util.DateUtil;
import factory.FactoryBoy;
import jobs.order.PrepaymentNotice;
import models.jobs.JobWithHistory;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Prepayment;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.mq.MockMQ;

import java.math.BigDecimal;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 12/6/12
 * Time: 10:39 AM
 */
public class PrepaymentNoticeUnitTest extends UnitTest {
    Prepayment prepayment;
    ECoupon eCoupon;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        JobWithHistory.cleanLastBeginRunAtForTest();

        eCoupon = FactoryBoy.create(ECoupon.class);
        prepayment = FactoryBoy.create(Prepayment.class);
        prepayment.expireAt = DateUtil.getEndOfDay();
        prepayment.save();
        eCoupon.originalPrice = new BigDecimal(9.5);
        eCoupon.consumedAt = DateUtil.getBeginOfDay();
        eCoupon.status = ECouponStatus.CONSUMED;
        eCoupon.save();
    }

    @Test
    public void testNoticeJob() throws Exception {
        new PrepaymentNotice().doJob();
        prepayment.refresh();
        assertTrue(prepayment.warning);

        MailMessage msg = (MailMessage) MockMQ.getLastMessage(MailUtil.COMMON_QUEUE);
        assertEquals("可用预付款已少于百分之十", msg.getSubject());

        assertEquals(prepayment.supplier.otherName, msg.getParam("supplier"));
    }

    @Test
    public void testNoNoticeJob() throws Exception {
        eCoupon.originalPrice = new BigDecimal(9);
        eCoupon.save();
        new PrepaymentNotice().doJob();
        prepayment.refresh();
        assertFalse(prepayment.warning);
    }

}
