package util.mq;

import models.mq.QueueIDMessage;
import models.mq.QueueIDRunType;
import play.Play;
import play.modules.rabbitmq.producer.RabbitMQPublisher;
import play.modules.redis.Redis;

/**
 * 用于包装MQPublisher，以支持测试时使用MockMQ.
 *
 * @author tanglq
 */
public class MQPublisher {

    /**
     * 用于在Redis中对发送消息进行计数，以实现最后加进的Message得到优先执行。
     */
    public final static String MESSAGE_COUNT_KEY = "mq.message.counts";

    public static void publish(String queue, Object message) {
        if (!Play.runingInTestMode()) {
            if (message instanceof QueueIDMessage) {
                recordMessage((QueueIDMessage) message);
            }
            RabbitMQPublisher.publish(queue, message);
        } else {
            MockMQ.publish(queue, message);
        }
    }

    public static void recordMessage(QueueIDMessage message) {
        Long messageCount = Redis.incr(MESSAGE_COUNT_KEY);
        if (message.getQueueIDRunType() == QueueIDRunType.ONLY_RUN_FIRST
                && Redis.exists(getMessageRedisId(message))) {
            return;
        }
        // 如果LastRun情况下，如果运行过程中加入了新的Queue，我们认为可以先不管
        Redis.zadd(getMessageRedisId(message), messageCount.doubleValue(), message.getUuid());
    }

    public static String getMessageRedisId(QueueIDMessage message) {
        return "queueid." + message.getId();
    }
}
