package models.taobao;

import play.Play;
import util.mq.MQPublisher;

import java.util.Map;

/**
 * User: yan
 * Date: 13-5-6
 * Time: 下午3:12
 */
public class KtvSkuMessageUtil {
    public static final String QUEUE_NAME = Play.mode.isProd() ? "ktv_sku" : "ktv_sku_dev";

    private KtvSkuMessageUtil() {
    }

    /**
     * 淘宝商品ID
     */
    public static void send(Long scheduledId) {
        MQPublisher.publish(QUEUE_NAME,scheduledId);
    }
}
