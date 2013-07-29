package models.baidu;

import play.Play;
import util.mq.MQPublisher;

/**
 * User: yan
 * Date: 13-7-12
 * Time: 下午9:51
 */
public class BaiduOrderMessageUtil {
    public static final String QUEUE_NAME = Play.mode.isProd() ? "baidu_order" : "baidu_order_dev";

    private BaiduOrderMessageUtil() {
    }

    public static void send(String outOrderId) {
        MQPublisher.publish(QUEUE_NAME, outOrderId);
    }
}
