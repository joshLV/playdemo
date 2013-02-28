package consumer.order;

import models.RabbitMQConsumerWithTx;
import models.order.CouponHistory;
import models.order.ECoupon;
import models.order.ECouponHistoryMessage;
import play.Logger;
import play.jobs.OnApplicationStart;
import util.transaction.RemoteRecallCheck;
import util.transaction.TransactionCallback;
import util.transaction.TransactionRetry;

/**
 * 保存ECouponHistory。
 * User: tanglq
 * Date: 13-1-23
 * Time: 上午11:32
 */
@OnApplicationStart(async = true)
public class ECouponHistoryConsumer extends RabbitMQConsumerWithTx<ECouponHistoryMessage> {
    @Override
    public void consumeWithTx(final ECouponHistoryMessage data) {
        // 使用事务重试
        RemoteRecallCheck.setId("ECouponHistory_" + data.eCouponId);
        Boolean success = TransactionRetry.run(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction() {
                return doCouponHistorySave(data);
            }
        });

        if (success == null || !success) {
            // 下次重试
            throw new RuntimeException("Not fund coupon(id:" + data.eCouponId + ")，it will auto try later.");
        }
    }

    private Boolean doCouponHistorySave(ECouponHistoryMessage data) {
        ECoupon coupon = ECoupon.findById(data.eCouponId);
        if (coupon == null) {
            Logger.info("Not fund coupon(id:" + data.eCouponId + ")，稍后自动重试.");
            throw new RuntimeException("Not fund coupon(id:" + data.eCouponId + ")，it will auto try later.");
        }
        Logger.info("process ECouponHistoryMessage:" + data);
        CouponHistory couponHistory = data.toModel();

        couponHistory.save();
        return Boolean.TRUE;
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
