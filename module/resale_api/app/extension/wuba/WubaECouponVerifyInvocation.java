package extension.wuba;

import extension.order.ECouponVerifyContext;
import extension.order.ECouponVerifyInvocation;
import models.order.ECouponPartner;
import models.wuba.WubaUtil;
import util.extension.ExtensionResult;

/**
 * User: tanglq
 * Date: 13-3-5
 * Time: 下午9:38
 */
public class WubaECouponVerifyInvocation extends ECouponVerifyInvocation {
    /**
     * 调用具体验证代码
     * @param context
     * @return ExtensionResult，我们约定如果ExtensionResult.code == 0，则为验证成功
     */
    @Override
    public ExtensionResult execute(ECouponVerifyContext context) {
        return WubaUtil.verifyOnWuba(context.eCoupon);
    }

    @Override
    public boolean match(ECouponVerifyContext context) {
        return context.isPartner(ECouponPartner.WB);
    }
}

