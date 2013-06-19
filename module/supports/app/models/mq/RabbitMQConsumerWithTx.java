package models.mq;

import play.Logger;
import play.db.jpa.JPAPlugin;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;
import play.modules.redis.Redis;
import play.modules.redis.RedisConnectionManager;
import util.mq.MQPublisher;
import util.transaction.RedisLock;

import java.util.Set;

public abstract class RabbitMQConsumerWithTx<T> extends RabbitMQConsumer<T> {

    @Override
    public final void consume(T t) {
        QueueIDMessage queueIDMessage = null;
        if (t instanceof QueueIDMessage) {
            queueIDMessage = (QueueIDMessage) t;
        }

        // 检查是否可执行
        if (queueIDMessage != null) {
            if (!Redis.exists(MQPublisher.getMessageRedisId(queueIDMessage))) {
                Logger.info("未找到需要运行的Redis Message Key，应该是已经运行好了，本次运行跳过。message:" + queueIDMessage);
                return;
            }
            if (queueIDMessage.queueIDRunType() == QueueIDRunType.LAST_IN_FIRST_RUN) {
                Set<String> uuidsNeedRun = Redis.zrevrange(MQPublisher.getMessageRedisId(queueIDMessage), 0, 0);
                if (!uuidsNeedRun.contains(queueIDMessage.getUuid())) {
                    Logger.info("当前消息不是需要执行的Message，跳过。message:" + queueIDMessage);
                    return;
                }
            }
        }

        JPAPlugin.startTx(false);

        boolean rollback = false;
        try {
            consumeWithTx(t);

            // 执行成功，清除queue id
            // 问题：在LAST_IN_FIRST_RUN情况下，如果运行过程中加入了新的Queue，我们认为可以先不管，直接清除掉此队列
            if (queueIDMessage != null) {
                Redis.del(new String[]{MQPublisher.getMessageRedisId(queueIDMessage)});
            }
        } catch (RuntimeException e) {
            rollback = true;
            throw e;
        } finally {
            JPAPlugin.closeTx(rollback);
            RedisConnectionManager.closeConnection();
        }
    }

    public abstract void consumeWithTx(T t);
}
