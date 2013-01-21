package unit;

import com.uhuila.common.util.DateUtil;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类的测试.
 * <p/>
 * User: sujie
 * Date: 4/6/12
 * Time: 4:02 PM
 */
public class DateUtilTest extends UnitTest {
    @Test
    public void testGetEndOfDay() {
        Date aDate = new Date();
        Date endDay = DateUtil.getEndOfDay(aDate);
        Calendar c = Calendar.getInstance();
        c.setTime(endDay);
        assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, c.get(Calendar.MINUTE));
        assertEquals(59, c.get(Calendar.SECOND));
        assertEquals(999, c.get(Calendar.MILLISECOND));
    }

    @Test
    public void testGetBeforeHour() {
        Calendar c = Calendar.getInstance();
        c.set(2013, 1, 1, 1, 0, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        Calendar c1 = Calendar.getInstance();
        c1.set(2013, 1, 1, 3, 0, 0);
        c1.set(Calendar.MINUTE, 0);
        c1.set(Calendar.SECOND, 0);
        c1.set(Calendar.MILLISECOND, 0);

        assertEquals(c.getTimeInMillis(), DateUtil.getBeforeHour(c1.getTime(), 2).getTime());
    }

}
