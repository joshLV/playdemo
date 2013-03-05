package extension.order;

import models.order.ECoupon;
import models.order.ECouponPartner;
import util.extension.ExtensionContext;

/**
 * User: tanglq
 * Date: 13-3-5
 * Time: 下午5:35
 */
public class ECouponVerifyContext implements ExtensionContext {

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
