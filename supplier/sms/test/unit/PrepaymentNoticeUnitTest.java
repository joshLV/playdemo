package unit;

import com.uhuila.common.util.DateUtil;
import factory.FactoryBoy;
import models.job.PrepaymentNotice;
import models.mail.MailMessage;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Prepayment;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.mq.MockMQ;
import models.mail.MailUtil;

import java.math.BigDecimal;
import java.text.ParseException;

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
    public void testNoticeJob() throws ParseException {
        new PrepaymentNotice().doJob();
        prepayment.refresh();
        assertTrue(prepayment.warning);

        MailMessage msg = (MailMessage) MockMQ.getLastMessage(MailUtil.COMMON_QUEUE);
        assertEquals("可用预付款已少于10%", msg.getSubject());

        assertEquals(prepayment.supplier.otherName, msg.getParam("supplier"));
    }

    @Test
    public void testNoNoticeJob() throws ParseException {
        eCoupon.originalPrice = new BigDecimal(9);
        eCoupon.save();
        new PrepaymentNotice().doJob();
        prepayment.refresh();
        assertFalse(prepayment.warning);
    }

}
