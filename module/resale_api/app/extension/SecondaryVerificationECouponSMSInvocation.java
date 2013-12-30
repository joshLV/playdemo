package extension;

import extension.order.OrderECouponSMSContext;
import extension.order.OrderECouponSMSInvocation;
import models.sales.Goods;
import org.apache.commons.lang.StringUtils;
import util.extension.ExtensionResult;

/**
 * User: yan
 * Date: 13-6-7
 * Time: 上午11:32
 */
public class SecondaryVerificationECouponSMSInvocation extends OrderECouponSMSInvocation {
    /**
     * 基于context的内容，生成短信内容，并通过context.setSmsContent()方法把短信内容传出.
     *
     * @param context
     * @return
     */
    @Override
    public ExtensionResult execute(OrderECouponSMSContext context) {
        StringBuilder sb = new StringBuilder();
        Goods goods = context.getGoods();
        sb.append("【")
                .append(goods.getSupplier().otherName)
                .append("】")
                .append(StringUtils.isNotEmpty(goods.title) ? goods.title : goods.shortName)
                .append(",")
                .append(context.couponInfo)
                .append(context.notes)
                .append("客服4006865151");
        context.setSmsContent(sb.toString());
        return ExtensionResult.SUCCESS;
    }

    /**
     * 检查是否是二次验证商品.
     *
     * @param context
     * @return
     */
    @Override
    public boolean match(OrderECouponSMSContext context) {
        return context.getGoods().isSecondaryVerificationGoods();
    }
}
