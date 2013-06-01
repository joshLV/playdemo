package consumer.taobao;

import consumer.TaobaoRabbitMQConsumerWithTx;
import models.ktv.KtvSkuTaobaoMessage;
import models.taobao.KtvSkuMessageUtil;
import play.jobs.OnApplicationStart;

/**
 * @author likang
 *         Date: 13-5-31
 */

@OnApplicationStart(async = true)
public class KtvSkuTaobaoActionConsumer0 extends TaobaoRabbitMQConsumerWithTx<KtvSkuTaobaoMessage> {
    private final KtvSkuTaobaoAction ktvSkuTaobaoAction = new KtvSkuTaobaoAction();

    @Override
    public void consumeWithTx(KtvSkuTaobaoMessage message) {
        ktvSkuTaobaoAction.processMessage(message);
    }

    @Override
    protected Class getMessageType() {
        return KtvSkuTaobaoMessage.class;
    }

    @Override
    protected String queue() {
        return KtvSkuMessageUtil.TAOBAO_SKU_QUEUE_NAME0;
    }
}
