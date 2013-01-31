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
    public void consumeWithTx(ECouponHistoryMessage data) {
        System.out.println(  "inini===>>");
        try {
            Thread.sleep(500l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("jere===>>");
        CouponHistory couponHistory = data.toModel();
        JPA.em().flush();
        System.out.println("jere1111===>>");
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
