package unit.queueid;

import models.mq.RabbitMQConsumerWithTx;
import play.modules.redis.Redis;

/**
 * User: tanglq
 * Date: 13-6-12
 * Time: 下午3:31
 */
public class SampleQueueIDMessageConsumer extends RabbitMQConsumerWithTx<SampleQueueIDMessage> {
    @Override
    public void consumeWithTx(SampleQueueIDMessage sampleQueueIDMessage) {
        try {
            Thread.sleep(500l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 递增一个值到Redis，测试时如果能够得到这个值，表明测试通过
        Redis.incr(sampleQueueIDMessage.getCountKey());
        // 记录下运行过的message，供测试检查
        Redis.set(sampleQueueIDMessage.getRunMessageKey(), sampleQueueIDMessage.getUuid());
    }

    @Override
    protected Class getMessageType() {
        return SampleQueueIDMessage.class;
    }

    @Override
    protected String queue() {
        return SampleQueueIDMessage.MQ_KEY;
    }
}
