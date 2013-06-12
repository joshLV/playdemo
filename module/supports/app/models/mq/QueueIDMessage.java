package models.mq;

import java.util.UUID;

/**
 * 用于标识Message对象的基类.
 *
 * RabbitMQConsumer收到此对象的基类时，会以getId()返回的值，到Redis库中去查找是否有此队列。
 */
public abstract class QueueIDMessage {

    protected String uuid;

    public QueueIDMessage() {
        uuid = UUID.randomUUID().toString();
    }

    public String getUuid() {
        return uuid;
    }

    public QueueIDRunType getQueueIDRunType() {
        return QueueIDRunType.ONLY_RUN_FIRST;
    }

    public abstract String getId();

}
