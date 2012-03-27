package models.resale.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class ResaleUtil {
	public static SimpleDateFormat df = new SimpleDateFormat( " yyyy-MM-dd " );
	/**   
	 * 得到本月的第一天和最后一天   
	 * @return   
	 */    
	public static Map<String, String> findThisMonth() {     
		Calendar calendar = Calendar.getInstance();     
		calendar.set(Calendar.DAY_OF_MONTH, calendar     
				.getActualMinimum(Calendar.DAY_OF_MONTH));

		Calendar lastCalendar = Calendar.getInstance();     
		lastCalendar.set(Calendar.DAY_OF_MONTH, calendar     
				.getActualMaximum(Calendar.DAY_OF_MONTH)); 

		Map<String, String> map = new HashMap<String, String>();
		map.put("thisMonthFD", df.format(calendar.getTime()));
		map.put("thisMonthLD", df.format(lastCalendar.getTime()));
		return map;     
	}     


	/**
	 * 得到上个月的第一和最后一天带时间
	 *
	 * @return MAP{prevMonthFD:当前日期上个月的第一天带时间}{prevMonthPD:当前日期上个月的最后一天带时间}
	 */
	public static Map<String, String> findLastMonth() {
		Calendar cal = Calendar.getInstance();
		GregorianCalendar gcLast = (GregorianCalendar) Calendar.getInstance();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		calendar.add(Calendar.MONTH, -1);
		Date theDate = calendar.getTime();
		gcLast.setTime(theDate);
		gcLast.set(Calendar.DAY_OF_MONTH, 1);
		String day_first_prevM = df.format(gcLast.getTime());
		StringBuffer str = new StringBuffer().append(day_first_prevM).append(
				" 00:00:00");
		day_first_prevM = str.toString();

		calendar.add(cal.MONTH, 1);
		calendar.set(cal.DATE, 1);
		calendar.add(cal.DATE, -1);
		String day_end_prevM = df.format(calendar.getTime());
		StringBuffer endStr = new StringBuffer().append(day_end_prevM).append(
				" 23:59:59");
		day_end_prevM = endStr.toString();


		Map<String, String> map = new HashMap<String, String>();
		map.put("prevMonthFD", day_first_prevM);
		map.put("prevMonthPD", day_end_prevM);
		return map;
	}
}
