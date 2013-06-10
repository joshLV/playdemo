package models.mq;

import play.db.jpa.JPAPlugin;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

public abstract class RabbitMQConsumerWithTx<T> extends RabbitMQConsumer<T> {

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
