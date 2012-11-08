package models.tsingtuan;

import play.Play;
import play.modules.rabbitmq.producer.RabbitMQPublisher;

public class TsingTuanSendOrder {
    
    public static final String SEND_ORDER = "TSINGTUAN_SEND_ORDER";
    public static final String REFUND_ORDER = "TSINGTUAN_REFUND_ORDER";
    
    /**
     * 订单生成通知.
     * @param order
     */
    public static void send(TsingTuanOrder order) {
        if (!Play.runingInTestMode()) {
            RabbitMQPublisher.publish(SEND_ORDER, order);
        }
    }

    /**
     * 退款通知.
     * @param order
     */
    public static void refund(TsingTuanOrder order) {
        if (!Play.runingInTestMode()) {
            RabbitMQPublisher.publish(REFUND_ORDER, order);
        }
    }
}
