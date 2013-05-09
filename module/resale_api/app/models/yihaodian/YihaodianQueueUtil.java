package models.yihaodian;

import play.Play;
import play.modules.rabbitmq.producer.RabbitMQPublisher;

/**
 * @author likang
 *         Date: 12-9-3
 */
public class YihaodianQueueUtil {
    public static final String QUEUE_NAME = Play.mode.isProd() ? "yihaodian_job" : "yihaodian_job_dev";

    public static final String GROUP_BUY_QUEUE_NAME = Play.mode.isProd() ? "yhd_group_buy_job" : "yhd_group_buy_job_dev";

    private YihaodianQueueUtil() {
    }

    public static void addJob(String orderId) {
        RabbitMQPublisher.publish(QUEUE_NAME, orderId);
    }

    public static void addGroupBuyJob(String orderId) {
        RabbitMQPublisher.publish(GROUP_BUY_QUEUE_NAME, orderId);
    }
}
