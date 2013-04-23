package extension.kangou;

import extension.order.OrderECouponSMSContext;
import extension.order.OrderECouponSMSInvocation;
import models.kangou.KangouCardStatus;
import models.kangou.KangouUtil;
import models.order.ECoupon;
import play.Logger;
import util.extension.ExtensionResult;

/**
 * User: tanglq
 * Date: 13-4-19
 * Time: 下午2:50
 */
public class KangouOrderECouponSMSInvocation extends OrderECouponSMSInvocation {
    /**
     * 基于context的内容，生成短信内容，并通过context.setSmsContent()方法把短信内容传出.
     *
     * @param context
     * @return
     */
    @Override
    public ExtensionResult execute(OrderECouponSMSContext context) {
        for (ECoupon eCoupon : context.orderItems.getECoupons()) {
            Logger.info("KangouOrderECouponSMSInvocation(eCoupon.id:" + eCoupon.id);
            KangouCardStatus kangouCardStatus = KangouUtil.setCardUseAndSend(eCoupon);
            Logger.info("    KangouOrderECouponSMSInvocation setCardUseAndSend(eCoupon:" + eCoupon.id + ") result:" +
                    kangouCardStatus);
        }
        context.needSendSMS = false; //不发短信.
        return ExtensionResult.SUCCESS;
    }

    /**
     * 检查是否是看购网订单.
     *
     * @param context
     * @return
     */
    @Override
    public boolean match(OrderECouponSMSContext context) {
        return KangouUtil.SUPPLIER_DOMAIN_NAME.equals(context.goods.getSupplier().domainName);
    }
}
