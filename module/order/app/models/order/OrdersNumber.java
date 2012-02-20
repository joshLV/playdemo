package models.order;

import org.apache.commons.lang.time.DateFormatUtils;

import java.util.Date;
import java.util.Random;

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
