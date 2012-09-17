package models.dangdang;

import play.Play;
import play.modules.rabbitmq.producer.RabbitMQPublisher;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-17
 * Time: 下午3:20
 */
public class DDOrderQueueUtil {
    public static final String QUEUE_NAME = Play.mode.isProd() ? "dangdang" : "dangdang_job_dev";

    private DDOrderQueueUtil() {
    }

    public static void addJob(DDOrderJobMessage message) {
        RabbitMQPublisher.publish(QUEUE_NAME, message);
    }
}
