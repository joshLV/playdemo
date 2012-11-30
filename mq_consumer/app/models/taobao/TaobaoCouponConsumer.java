package models.taobao;

import models.order.ECoupon;
import models.order.OuterOrder;
import models.taobao_coupon.TaobaoCouponUtil;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

import java.util.List;

/**
 * @author likang
 *         Date: 12-11-29
 */
@OnApplicationStart(async = true)
public class TaobaoCouponConsumer extends RabbitMQConsumer<TaobaoCouponMessage>{
    @Override
    protected void consume(TaobaoCouponMessage taobaoCouponMessage) {
        OuterOrder outerOrder = OuterOrder.findById(taobaoCouponMessage.outerOrderId);
        if (TaobaoCouponUtil.ACTION_SEND.equals(outerOrder.extra)) {
            TaobaoCouponUtil.tellTaobaoCouponSend(outerOrder);
            outerOrder.extra = null;
            outerOrder.save();
        }else if (TaobaoCouponUtil.ACTION_RESEND.equals(outerOrder.extra)) {
            if(TaobaoCouponUtil.tellTaobaoCouponResend(outerOrder)) {
                List<ECoupon> eCoupons = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
                for(ECoupon eCoupon: eCoupons) {
                    if (eCoupon.downloadTimes > 0) {
                        ECoupon.send(eCoupon, eCoupon.orderItems.phone);
                        eCoupon.downloadTimes -= 1;
                        eCoupon.save();
                    }
                }
            }
            outerOrder.extra = null;
            outerOrder.save();
        }
    }

    @Override
    protected Class getMessageType() {
        return TaobaoCouponMessage.class;
    }

    @Override
    protected String queue() {
        return TaobaoCouponMessageUtil.QUEUE_NAME;
    }
}
