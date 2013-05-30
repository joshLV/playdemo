package models.taobao;

import models.ktv.KtvProductGoods;
import models.ktv.KtvSkuTaobaoMessage;
import play.Play;
import util.mq.MQPublisher;

import java.util.Map;

/**
 * User: yan
 * Date: 13-5-6
 * Time: 下午3:12
 */
public class KtvSkuMessageUtil {
    public static final String QUEUE_NAME = Play.mode.isProd() ? "taobao_ktv_sku" : "taobao_ktv_sku_dev";

    public static final String TAOBAO_SKU_QUEUE_NAME = Play.mode.isProd() ? "ktv_taobao_sku_action" : "ktv_taobao_sku_action_dev";

    public static int ACTION_ADD = 1;
    public static int ACTION_UPDATE = 2;
    public static int ACTION_DELETE = 3;


    private KtvSkuMessageUtil() {
    }

    /**
     * 淘宝商品ID
     */
    public static void send(Long scheduledId) {
        KtvSkuMessage message = new KtvSkuMessage(scheduledId);
        MQPublisher.publish(QUEUE_NAME, message);
    }

    public static void sendTaobaoAction(KtvSkuTaobaoMessage message) {
        if (message.resalerProductId == null) {
            throw  new IllegalArgumentException("resalerProductId is null");
        }
        MQPublisher.publish(TAOBAO_SKU_QUEUE_NAME, message);
    }
}
