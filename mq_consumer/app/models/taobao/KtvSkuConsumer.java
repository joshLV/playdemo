package models.taobao;

import models.RabbitMQConsumerWithTx;
import models.ktv.KtvProductGoods;
import models.ktv.KtvTaobaoUtil;
import play.Logger;
import play.jobs.OnApplicationStart;

/**
 * User: yan
 * Date: 13-5-6
 * Time: 下午3:18
 */
@OnApplicationStart(async = true)
public class KtvSkuConsumer extends RabbitMQConsumerWithTx<KtvSkuMessage> {
    @Override
    public void consumeWithTx(KtvSkuMessage message) {
        try {
            Thread.sleep(2000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Logger.info("message.ktvProductGoodsId:%s,message.productGoodsId:%s", message.productGoodsId);
        //根据价格策略更新sku
        if (message.productGoodsId != null) {
            KtvProductGoods productGoods = KtvProductGoods.findById(message.productGoodsId);
            if (productGoods != null) {
                KtvTaobaoUtil.updateTaobaoSkuByProductGoods(productGoods);
            }
        } else {
            Logger.info("KtvSkuConsumer process faild:message.scheduleId is null");
        }
    }

    @Override
    protected Class getMessageType() {
        return KtvSkuMessage.class;
    }

    @Override
    protected String queue() {
        return KtvSkuMessageUtil.QUEUE_NAME;
    }
}
