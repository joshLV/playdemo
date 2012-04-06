package com.uhuila.common.util;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类的测试.
 * <p/>
 * User: sujie
 * Date: 4/6/12
 * Time: 4:02 PM
 */
public class DateUtilTest extends TestCase {
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

}
