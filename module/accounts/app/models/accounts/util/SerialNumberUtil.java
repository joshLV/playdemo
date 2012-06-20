package models.accounts.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * User: likang
 */
public class SerialNumberUtil {
    private static final String DATE_FORMAT = "yyyyMMddhhmmssSSS";
    private static final String DECIMAL_FORMAT = "00000";
    
    public static String generateSerialNumber(){
        return generateSerialNumber(new Date());
    }
    
    public static String generateSerialNumber(Date date){
        int random = new Random().nextInt(100000);

        //SimpleDateFormat和DecimalFormat不是线程安全的
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        DecimalFormat decimalFormat = new DecimalFormat(DECIMAL_FORMAT);
        return dateFormat.format(date) + decimalFormat.format(random);
    }
}
