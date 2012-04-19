package com.uhuila.common.util;

import java.util.Random;

public class RadomNumberUtil {
	private static String chars = "0123456789";

	/**
	 * 产生指定位数的随机数字
	 * @param length 长度
	 * @return 随机数
	 */
	public static String generateSerialNumber(int length) {
		char[] charsArray = chars.toCharArray();
		Random random = new Random(System.currentTimeMillis());
		StringBuffer sb = new StringBuffer(length);
		for (int i = 0; i < length; i++) {
			sb.append(charsArray[random.nextInt(charsArray.length)]);
		}
		String text = sb.toString();
		return text;
	}
}
