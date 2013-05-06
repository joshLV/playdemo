package models.sina;

import play.Play;
import util.mq.MQPublisher;

/**
 * User: yan
 * Date: 13-3-26
 * Time: 下午1:30
 */
public class SinaVouchersMessageUtil {
    public static final String QUEUE_NAME = Play.mode.isProd() ? "sina_vouchers" : "sina_vouchers_dev";

    private SinaVouchersMessageUtil() {
    }

    public static void send(Long couponId) {
        MQPublisher.publish(QUEUE_NAME, couponId);
    }
}
