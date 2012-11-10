package util.mq;

import play.Play;
import play.modules.rabbitmq.producer.RabbitMQPublisher;

/**
 *  用于包装MQPublisher，以支持测试时使用MockMQ.
 * @author tanglq
 *
 */
public class MQPublisher {

    public static void publish(String queue, Object message) {
        if (!Play.runingInTestMode()) {
            RabbitMQPublisher.publish(queue, message);
        } else {
            MockMQ.publish(queue, message);
        }
    }
}
