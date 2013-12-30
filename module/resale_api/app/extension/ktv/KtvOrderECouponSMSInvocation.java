package extension.ktv;

import extension.order.OrderECouponSMSContext;
import extension.order.OrderECouponSMSInvocation;
import util.extension.ExtensionResult;

/**
 * 实现重定义ktv商品的订单券短信内容。
 */
public class KtvOrderECouponSMSInvocation extends OrderECouponSMSInvocation {
    /**
     * 基于context的内容，生成短信内容，并通过context.setSmsContent()方法把短信内容传出.
     *
     * @param context
     * @return
     */
    @Override
    public ExtensionResult execute(OrderECouponSMSContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getGoods().getSupplier().otherName)
                .append("：")
                .append(context.getGoods().title)
                .append(context.couponInfo)
                .append(",客服4006865151");
        context.setSmsContent(sb.toString());
        return ExtensionResult.SUCCESS;
    }

    /**
     * 检查是否是KTV商户并且是KTV商品生成的订单.
     *
     * @param context
     * @return
     */
    @Override
    public boolean match(OrderECouponSMSContext context) {
        return context.getGoods().isKtvProduct();
    }
}
