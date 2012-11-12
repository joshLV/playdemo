package models;

import play.db.jpa.JPAPlugin;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

public abstract class RabbitMQConsumerWithTx<T> extends RabbitMQConsumer<T> {

    @Override
    protected final void consume(T t) {
        JPAPlugin.startTx(false);
        
        try {
            consumeWithTx(t);
        } finally {
            JPAPlugin.closeTx(false);
        }
    }

    public abstract void consumeWithTx(T t);
}
