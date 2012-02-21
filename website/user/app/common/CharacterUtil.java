package common;

import java.util.Random;

/**
 * 共通方法
 * 
 * @author yanjy
 *
 */
public class CharacterUtil {

	/**
	 * 产生随机数
	 * @param length
	 * @return
	 */
	public static String getRandomString(int length) {
		String str="abcdefghigklmnopkrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789";
		Random random=new Random();
		StringBuffer sf=new StringBuffer();
		for(int i=0;i<length;i++)	{
			int number=random.nextInt(62);//0~61
			sf.append(str.charAt(number));
		}
		return sf.toString();
	}
}
