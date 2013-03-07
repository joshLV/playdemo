package extension.taobao;

import extension.order.ECouponVerifyContext;
import extension.order.ECouponVerifyInvocation;
import models.order.ECouponPartner;
import models.taobao.TaobaoCouponUtil;
import util.extension.ExtensionResult;

/**
 * 淘宝券验证接口实现.
 */
public class TaobaoECouponVerifyInvocation extends ECouponVerifyInvocation {
    /**
     * 调用具体验证代码
     * @param context
     * @return ExtensionResult，我们约定如果ExtensionResult.code == 0，则为验证成功
     */
    @Override
    public ExtensionResult execute(ECouponVerifyContext context) {
        return TaobaoCouponUtil.verifyOnTaobao(context.eCoupon);
    }

    @Override
    public boolean match(ECouponVerifyContext context) {
        return context.isPartner(ECouponPartner.TB);
    }
}
