package com.uhuila.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    public static SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public static Date getEndOfDay(Date day) {
        if (day == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public static Date getBeginOfDay(Date day) {
        if (day == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 1);
        return calendar.getTime();
    }

    public static Date getBeginOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 000);
        return calendar.getTime();
    }

    /**
     * n天后的结束时间
     *
     * @return
     * @throws ParseException
     */
    public static Date getEndExpiredDate(int n) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, +n);
        Date date = DateUtil.getEndOfDay(cal.getTime());
        return date;
    }

    /**
     * n天后的开始时间
     *
     * @return
     * @throws ParseException
     */
    public static Date getBeginExpiredDate(int n) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, +n);
        Date date = DateUtil.getEndOfDay(cal.getTime());
        return date;
    }

    public static Date getYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 判断是否还有7天到期
     *
     * @param date 截止日期
     * @return true false
     * @throws ParseException
     */
    public static boolean getDiffDate(Date date) {
        long dateRange = 0l;
        long time = 1000 * 3600 * 24; //A day in milliseconds
        String now = format.format(new Date());
        Date sysDate = null;
        try {
            sysDate = format.parse(now);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dateRange = date.getTime() - sysDate.getTime();
        if (dateRange / time == 7) {
            return true;
        }

        return false;
    }

    /**
     * 当前时间
     *
     * @return
     */
    public static String getNowTime() {

        Calendar c = Calendar.getInstance();
        String nowTime = c.get(Calendar.MONTH) + 1 + "月" + c.get(Calendar.DATE) + "日" + c.get(Calendar.HOUR_OF_DAY) + "时" + c
                .get(Calendar.MINUTE) + "分";

        return nowTime;
    }

}
