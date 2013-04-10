package extension.sina;

import extension.order.ECouponVerifyContext;
import extension.order.ECouponVerifyInvocation;
import models.jingdong.groupbuy.JDGroupBuyHelper;
import models.order.ECouponPartner;
import models.sina.SinaVoucherUtil;
import util.extension.ExtensionResult;

/**
 * 新浪券验证调用接口。
 *
 */
public class SinaECouponVerifyInvocation extends ECouponVerifyInvocation {
    /**
     * 调用具体验证代码
     * @param context
     * @return ExtensionResult，我们约定如果ExtensionResult.code == 0，则为验证成功
     */
    @Override
    public ExtensionResult execute(ECouponVerifyContext context) {
        return SinaVoucherUtil.disposeCoupon(context.eCoupon);
    }

    @Override
    public boolean match(ECouponVerifyContext context) {
        return context.isPartner(ECouponPartner.SINA);
    }
}
