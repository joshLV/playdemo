package consumer.order;

import models.RabbitMQConsumerWithTx;
import models.order.CouponHistory;
import models.order.ECouponHistoryData;
import play.db.jpa.JPA;

/**
 * 保存ECouponHistory。
 * User: tanglq
 * Date: 13-1-23
 * Time: 上午11:32
 */
public class ECouponHistoryConsumer extends RabbitMQConsumerWithTx<ECouponHistoryData> {
    @Override
    public void consumeWithTx(ECouponHistoryData data) {
        CouponHistory couponHistory = data.toModel();
        JPA.em().flush();
        couponHistory.save();
    }

    @Override
    protected Class getMessageType() {
        return ECouponHistoryData.class;
    }

    @Override
    protected String queue() {
        return ECouponHistoryData.MQ_KEY;
    }
}
