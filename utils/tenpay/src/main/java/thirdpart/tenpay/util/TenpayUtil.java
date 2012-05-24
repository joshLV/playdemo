package thirdpart.tenpay.util;

import play.Play;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

public class TenpayUtil {
    public static final String APP_ID = Play.configuration.getProperty("tenpay.app_id","1211869101");
    public static final String SECRET_KEY = Play.configuration.getProperty("tenpay.key","cdaae5034d86383038eddcd1b8834c89");
    public static final String NOTIFY_URL = Play.configuration.getProperty("tenpay.notify_url");
    public static final String RETURN_URL = Play.configuration.getProperty("tenpay.return_url");
    public static final String PAY_GATE_URL = "https://gw.tenpay.com/gateway/pay.htm";
    public static final String VERIFY_GATE_URL = "https://gw.tenpay.com/gateway/simpleverifynotifyid.xml";

    public static void addSign(SortedMap<String, String> params){
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, String> entry : params.entrySet()){
            String k = entry.getKey();
            String v = entry.getValue();
            if(null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + TenpayUtil.SECRET_KEY);
        String enc = TenpayUtil.getCharacterEncoding();
        String sign = MD5Util.MD5Encode(sb.toString(), enc).toLowerCase();
        params.put("sign", sign);
    }

    public static String getRequestUrl(SortedMap<String, String> params, String preUrl) {
        addSign(params);
        StringBuilder sb = new StringBuilder();
        String enc = TenpayUtil.getCharacterEncoding();

        try{
            for(Map.Entry<String, String> entry : params.entrySet()){
                String k = entry.getKey();
                String v = entry.getValue();
                sb.append(k + "=" + URLEncoder.encode(v, enc) + "&");
            }}catch (UnsupportedEncodingException e){
            return "";
        }

        //去掉最后一个&
        String reqPars = sb.substring(0, sb.lastIndexOf("&"));
        return preUrl + "?" + reqPars;
    }


    /**
     * 是否财付通签名,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
     * @return boolean
     */
    public static boolean isTenpaySign(SortedMap<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, String> entry : params.entrySet()){
            String k = entry.getKey();
            String v = entry.getValue();
            if(!"sign".equals(k) && null != v && !"".equals(v)) {
                sb.append(k + "=" + v + "&");
            }
        }

        sb.append("key=" + SECRET_KEY);

        //算出摘要
        String enc = TenpayUtil.getCharacterEncoding();
        String sign = MD5Util.MD5Encode(sb.toString(), enc).toLowerCase();
        String tenpaySign = params.get("sign").toLowerCase();

        return tenpaySign.equals(sign);
    }

	/**
	 * 把对象转换成字符串
	 * @param obj
	 * @return String 转换成字符串,若对象为null,则返回空字符串.
	 */
	public static String toString(Object obj) {
		if(obj == null)
			return "";
		
		return obj.toString();
	}
	
	/**
	 * 把对象转换为int数值.
	 * 
	 * @param obj
	 *            包含数字的对象.
	 * @return int 转换后的数值,对不能转换的对象返回0。
	 */
	public static int toInt(Object obj) {
		int a = 0;
		try {
			if (obj != null)
				a = Integer.parseInt(obj.toString());
		} catch (Exception e) {

		}
		return a;
	}
	
	/**
	 * 获取当前时间 yyyyMMddHHmmss
	 * @return String
	 */ 
	public static String getCurrTime() {
		Date now = new Date();
		SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String s = outFormat.format(now);
		return s;
	}
	
	/**
	 * 获取当前日期 yyyyMMdd
	 * @param date
	 * @return String
	 */
	public static String formatDate(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		String strDate = formatter.format(date);
		return strDate;
	}
	
	/**
	 * 取出一个指定长度大小的随机正整数.
	 * 
	 * @param length
	 *            int 设定所取出随机数的长度。length小于11
	 * @return int 返回生成的随机数。
	 */
	public static int buildRandom(int length) {
		int num = 1;
		double random = Math.random();
		if (random < 0.1) {
			random = random + 0.1;
		}
		for (int i = 0; i < length; i++) {
			num = num * 10;
		}
		return (int) ((random * num));
	}
	
	/**
	 * 获取编码字符集
	 * @return String
	 */
	public static String getCharacterEncoding() {
        return Play.defaultWebEncoding;
	}
	
	/**
	 * 获取unix时间，从1970-01-01 00:00:00开始的秒数
	 * @param date
	 * @return long
	 */
	public static long getUnixTime(Date date) {
		if( null == date ) {
			return 0;
		}
		
		return date.getTime()/1000;
	}
		
	/**
	 * 时间转换成字符串
	 * @param date 时间
	 * @param formatType 格式化类型
	 * @return String
	 */
	public static String date2String(Date date, String formatType) {
		SimpleDateFormat sdf = new SimpleDateFormat(formatType);
		return sdf.format(date);
	}

}
