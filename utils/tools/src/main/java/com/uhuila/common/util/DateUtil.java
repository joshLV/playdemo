package com.uhuila.common.util;

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
	public static SimpleDateFormat df = new SimpleDateFormat( "HH:mm:ss" );
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
	public static void main(String[] args){
			System.out.println(getTimeRegion());
		
		
	}
	/**
	 * 判断当日的时间是否在11点和14点之间
	 * 
	 * @return 在该范围内：true 
	 */
	public static boolean getTimeRegion() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		String date= df.format(calendar.getTime());
		if (date.compareTo("09:00:00")>0 && date.compareTo("18:00:00")<0) {
			return true;
		}
		return false;
	}
}
