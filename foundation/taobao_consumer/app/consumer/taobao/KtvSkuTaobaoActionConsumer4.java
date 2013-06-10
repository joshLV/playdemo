package consumer.taobao;

import models.mq.RabbitMQConsumerWithTx;
import models.taobao.KtvSkuMessageUtil;
import play.jobs.OnApplicationStart;

/**
 * @author likang
 *         Date: 13-5-31
 */

@OnApplicationStart(async = true)
public class KtvSkuTaobaoActionConsumer4 extends RabbitMQConsumerWithTx<Long> {
    private final KtvSkuTaobaoAction ktvSkuTaobaoAction = new KtvSkuTaobaoAction();

    @Override
    public void consumeWithTx(Long ktvProductGoodsId) {
        ktvSkuTaobaoAction.processMessage(ktvProductGoodsId);
    }

    @Override
    protected Class getMessageType() {
        return Long.class;
    }

    @Override
    protected String queue() {
        return KtvSkuMessageUtil.TAOBAO_SKU_QUEUE_NAME4;
    }
}

