package unit;

import com.uhuila.common.util.DateUtil;
import factory.FactoryBoy;
import models.order.ECoupon;
import models.order.Prepayment;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.text.ParseException;
import java.util.Date;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 12/6/12
 * Time: 10:39 AM
 */
public class PrepaymentNoticeUnitTest extends UnitTest {
    Prepayment prepayment;
    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        ECoupon eCoupon = FactoryBoy.create(ECoupon.class);
        prepayment = FactoryBoy.create(Prepayment.class);
    }

    @Test
    public void testJob() throws ParseException {
        prepayment.expireAt = DateUtil.getEndOfDay(new Date());
        prepayment.save();
    }
}
