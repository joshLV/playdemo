package extension.order;

import models.order.ECoupon;

/**
 * User: tanglq
 * Date: 13-3-5
 * Time: 下午5:35
 */
public class ECouponVerifyContext {

    public ECoupon eCoupon;

    private ECouponVerifyContext() {}

    public static ECouponVerifyContext build() {
        return new ECouponVerifyContext();
    }

}
