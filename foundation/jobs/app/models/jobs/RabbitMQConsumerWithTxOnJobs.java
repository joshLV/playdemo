package models.jobs;

import play.db.jpa.JPAPlugin;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

/**
 * User: tanglq
 * Date: 13-5-4
 * Time: 下午1:47
 */
public abstract class RabbitMQConsumerWithTxOnJobs<T> extends RabbitMQConsumer<T> {

    @Override
    protected final void consume(T t) {
        JPAPlugin.startTx(false);

        boolean rollback = false;
        try {
            consumeWithTx(t);
        } catch (RuntimeException e) {
            rollback = true;
            throw e;
        } finally {
            JPAPlugin.closeTx(rollback);
        }
    }

    public abstract void consumeWithTx(T t);
}
