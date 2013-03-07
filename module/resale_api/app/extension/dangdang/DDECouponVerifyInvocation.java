package extension.dangdang;

import extension.order.ECouponVerifyContext;
import extension.order.ECouponVerifyInvocation;
import models.dangdang.groupbuy.DDGroupBuyUtil;
import models.order.ECouponPartner;
import util.extension.ExtensionResult;

/**
 * 京东券验证调用接口。
 */
public class DDECouponVerifyInvocation extends ECouponVerifyInvocation {
    /**
     * 调用具体验证代码
     * @param context
     * @return ExtensionResult，我们约定如果ExtensionResult.code == 0，则为验证成功
     */
    @Override
    public ExtensionResult execute(ECouponVerifyContext context) {
        return DDGroupBuyUtil.verifyOnDangdang(context.eCoupon);
    }

    @Override
    public boolean match(ECouponVerifyContext context) {
        return context.isPartner(ECouponPartner.DD);
    }
}
