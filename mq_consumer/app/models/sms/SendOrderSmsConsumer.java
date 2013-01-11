package models.sms;

import models.RabbitMQConsumerWithTx;
import models.order.Order;

/**
 * 发送订单的短信，如果有多个短信，会一次发掉。
 */
public class SendOrderSmsConsumer extends RabbitMQConsumerWithTx<Long> {
    @Override
    public void consumeWithTx(Long orderId) {
        Order order = Order.findById(orderId);
    }

    @Override
    protected Class getMessageType() {
        return Long.class;
    }

    @Override
    protected String queue() {
        return SMSUtil.SMS_ORDER_QUEUE;
    }
}
