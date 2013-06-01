package models.taobao;

import models.ktv.KtvSkuTaobaoMessage;
import play.Play;
import util.mq.MQPublisher;

import java.util.Random;

/**
 * User: yan
 * Date: 13-5-6
 * Time: 下午3:12
 */
public class KtvSkuMessageUtil {
    public static final String QUEUE_NAME = Play.mode.isProd() ? "taobao_ktv_sku" : "taobao_ktv_sku_dev";

    public static final String TAOBAO_SKU_QUEUE_NAME = Play.mode.isProd() ? "ktv_taobao_sku_action" : "ktv_taobao_sku_action_dev";
    public static final String TAOBAO_SKU_QUEUE_NAME0 = TAOBAO_SKU_QUEUE_NAME + "0";
    public static final String TAOBAO_SKU_QUEUE_NAME1 = TAOBAO_SKU_QUEUE_NAME + "1";
    public static final String TAOBAO_SKU_QUEUE_NAME2 = TAOBAO_SKU_QUEUE_NAME + "2";
    public static final String TAOBAO_SKU_QUEUE_NAME3 = TAOBAO_SKU_QUEUE_NAME + "3";
    public static final String TAOBAO_SKU_QUEUE_NAME4 = TAOBAO_SKU_QUEUE_NAME + "4";
    public static final String TAOBAO_SKU_QUEUE_NAME5 = TAOBAO_SKU_QUEUE_NAME + "5";

    public static int ACTION_ADD = 1;
    public static int ACTION_UPDATE = 2;
    public static int ACTION_DELETE = 3;


    private KtvSkuMessageUtil() {
    }

    /**
     * 淘宝商品ID
     */
    public static void send(Long productGoodsId) {
        KtvSkuMessage message = new KtvSkuMessage(productGoodsId);
        MQPublisher.publish(QUEUE_NAME, message);
    }

    public static void sendTaobaoAction(KtvSkuTaobaoMessage message) {
        if (message.resalerProductId == null) {
            throw  new IllegalArgumentException("resalerProductId is null");
        }
        Random random = new Random(System.currentTimeMillis());

        MQPublisher.publish(TAOBAO_SKU_QUEUE_NAME + random.nextInt(6), message);
    }
}
