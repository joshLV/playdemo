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
            Thread.sleep(5000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Logger.info("message.ktvProductGoodsId:%s,message.scheduledId:%s", message.ktvProductGoodsId, message.scheduledId);
        //根据价格策略更新sku
        if (message.scheduledId != null) {
            KtvTaobaoUtil.updateTaobaoSkuByPriceSchedule(message.scheduledId);
//        } else {
//            //根据ktv产品更新sku
//            KtvProductGoods ktvProductGoods = KtvProductGoods.findById(message.ktvProductGoodsId);
//            if (ktvProductGoods != null) {
//                KtvTaobaoUtil.updateTaobaoSkuByProductGoods(ktvProductGoods);
//            } else {
//                Logger.info("KtvSkuConsumer process faild:ktvProductGoods is null");
//            }
        }

        Logger.info("KtvSkuConsumer process faild:message.scheduleId is null");
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
