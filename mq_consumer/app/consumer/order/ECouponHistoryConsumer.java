package consumer.order;

import models.RabbitMQConsumerWithTx;
import models.order.CouponHistory;
import models.order.ECouponHistoryMessage;
import play.db.jpa.JPA;
import play.jobs.OnApplicationStart;

/**
 * 保存ECouponHistory。
 * User: tanglq
 * Date: 13-1-23
 * Time: 上午11:32
 */
@OnApplicationStart(async = true)
public class ECouponHistoryConsumer extends RabbitMQConsumerWithTx<ECouponHistoryMessage> {
    @Override
    public void consumeWithTx(ECouponHistoryMessage data)  {
        try {
            Thread.sleep(500l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CouponHistory couponHistory = data.toModel();
        JPA.em().flush();
        couponHistory.save();
    }

    @Override
    protected Class getMessageType() {
        return ECouponHistoryMessage.class;
    }

    @Override
    protected String queue() {
        return ECouponHistoryMessage.MQ_KEY;
    }
}
