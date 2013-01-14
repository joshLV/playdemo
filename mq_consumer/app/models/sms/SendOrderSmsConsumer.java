package models.sms;

import models.RabbitMQConsumerWithTx;
import play.db.jpa.JPA;

/**
 * 发送订单的短信，如果有多个短信，会一次发掉。
 */
public class SendOrderSmsConsumer extends RabbitMQConsumerWithTx<OrderECouponMessage> {
    @Override
    public void consumeWithTx(OrderECouponMessage message) {
        JPA.em().flush();  // 先强制同步hibernate缓存，避免找不到数据的情况; 如果还找不到，放异常以重试

        if (message.eCouponId != null) {

        } else if (message.orderItemId != null) {

        } else if (message.orderId != null) {

        }

    }

    @Override
    protected Class getMessageType() {
        return OrderECouponMessage.class;
    }

    @Override
    protected String queue() {
        return SMSUtil.SMS_ORDER_QUEUE;
    }
}
