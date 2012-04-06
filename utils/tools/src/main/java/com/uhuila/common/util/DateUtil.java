package com.uhuila.common.util;

import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类.
 * <p/>
 * User: sujie
 * Date: 4/6/12
 * Time: 3:57 PM
 */
public class DateUtil {

    public static Date getEndOfDay(Date day) {
        if (day == null){
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        calendar.set(Calendar.MILLISECOND,999);
        return calendar.getTime();
    }
}
