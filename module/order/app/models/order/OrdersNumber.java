package models.order;

import java.util.Date;
import java.util.Random;

import org.apache.commons.lang.time.DateFormatUtils;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 2/20/12
 * Time: 4:42 PM
 */
public class OrdersNumber {
    public synchronized static String generateOrderNumber() {
        int random = new Random().nextInt() % 10000;
        return "" + DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS") + random;
    }

}
