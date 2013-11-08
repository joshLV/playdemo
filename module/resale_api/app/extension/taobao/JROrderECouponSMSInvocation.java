package extension.taobao;

import extension.order.OrderECouponSMSContext;
import extension.order.OrderECouponSMSInvocation;
import models.resale.Resaler;
import models.sales.Goods;
import org.apache.commons.lang.StringUtils;
import util.extension.ExtensionResult;

/**
 * 实现重定义精锐教育团购的订单券短信内容。
 */
public class JROrderECouponSMSInvocation extends OrderECouponSMSInvocation {
    /**
     * 基于context的内容，生成短信内容，并通过context.setSmsContent()方法把短信内容传出.
     *
     * @param context
     * @return
     */
    @Override
    public ExtensionResult execute(OrderECouponSMSContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("【")
                .append(context.getGoods().getSupplier().otherName)
                .append("】")
                .append(context.getGoods().title)
                .append(context.couponInfo)
                .append("精锐咨询400-094-7770");
        context.setSmsContent(sb.toString());
        return ExtensionResult.SUCCESS;
    }

    /**
     * 检查是否是精锐团生成的订单.
     *
     * @param context
     * @return
     */
    @Override
    public boolean match(OrderECouponSMSContext context) {
        return context.getGoods().supplierId.equals(837L);
    }
}
