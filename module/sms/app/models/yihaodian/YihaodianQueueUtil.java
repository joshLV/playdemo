package models.yihaodian;

import play.Play;
import play.modules.rabbitmq.producer.RabbitMQPublisher;

/**
 * @author likang
 *         Date: 12-9-3
 */
public class YihaodianQueueUtil {
    public static final String YIHAODIAN_JOB = Play.mode.isProd() ? "yihaodian_job" : "yihaodian_job_dev";

    private YihaodianQueueUtil() {
    }

    public static void addJob(YihaodianJobMessage message) {
        RabbitMQPublisher.publish(YIHAODIAN_JOB, message);
    }
}
