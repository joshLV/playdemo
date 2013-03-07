package extension.order;

import models.order.ECoupon;
import models.order.ECouponPartner;
import util.extension.InvocationContext;

/**
 * 券验证检查上下文.
 */
public class ECouponVerifyContext implements InvocationContext {

    public ECoupon eCoupon;

    private ECouponVerifyContext() {
    }

    public static ECouponVerifyContext build(ECoupon coupon) {
        ECouponVerifyContext context = new ECouponVerifyContext();
        context.eCoupon = coupon;

        return context;
    }

    /**
     * 检查是否是指定的合作渠道.
     * @param targetPartner
     * @return
     */
    public boolean isPartner(ECouponPartner targetPartner) {
        return eCoupon.partner != null && eCoupon.partner == targetPartner;
    }

}
