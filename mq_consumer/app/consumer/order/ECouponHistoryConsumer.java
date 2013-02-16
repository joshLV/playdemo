package consumer.order;

import models.RabbitMQConsumerWithTx;
import models.order.CouponHistory;
import models.order.ECoupon;
import models.order.ECouponHistoryMessage;
import play.Logger;
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
        ECoupon coupon = ECoupon.findById(data.eCouponId);
        if (coupon == null) {
            try {
                Thread.sleep(500l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Logger.info("Not fund coupon(id:" + data.eCouponId + ")，稍后自动重试.");
            throw new RuntimeException("Not fund coupon(id:" + data.eCouponId + ")，it will auto try later.");
        }
        Logger.info("process ECouponHistoryMessage:" + data);
        CouponHistory couponHistory = data.toModel();

        // JPA.em().flush();
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
