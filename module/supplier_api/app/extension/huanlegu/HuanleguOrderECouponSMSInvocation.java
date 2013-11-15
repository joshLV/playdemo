package extension.huanlegu;

import extension.order.OrderECouponSMSContext;
import extension.order.OrderECouponSMSInvocation;
import models.huanlegu.HuanleguUtil;
import models.sales.Goods;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import util.extension.ExtensionResult;

/**
 * User: tanglq
 * Date: 13-4-19
 * Time: 下午2:50
 */
public class HuanleguOrderECouponSMSInvocation extends OrderECouponSMSInvocation {
    //淘宝、京东、58、一号店、一百券
    public static String[] PARTNER_UIDS = Play.configuration.getProperty("auto_partners_uids", "13,34,10,14,7,22").split(",");

    /**
     * 基于context的内容，生成短信内容，并通过context.setSmsContent()方法把短信内容传出.
     *
     * @param context
     * @return
     */
    @Override
    public ExtensionResult execute(OrderECouponSMSContext context) {
        context.needSendSMS = ArrayUtils.contains(PARTNER_UIDS, String.valueOf(context.getOrder().userId));

        if (context.needSendSMS) {
            Goods goods = context.getGoods();
            StringBuilder sb = new StringBuilder();

            sb.append(StringUtils.isNotEmpty(goods.title) ? goods.title : goods.shortName)
                    .append(context.couponInfo)
                    .append(context.notes)
                    .append("[需预约]")
                    .append("预约电话4006865151");

            context.setSmsContent(sb.toString());
            Logger.info("huanlegu coupon sms: mobile %s, content: %s",
                    context.getOrderItem().phone, context.getSmsContent());
        }else {
            Logger.info("huanlegu coupon sms: skip coupon_sn %s mobile %s",
                    context.getFirstCoupon().eCouponSn,  context.getOrderItem().phone );
        }

        return ExtensionResult.SUCCESS;
    }


    /**
     * 检查是否是欢乐谷订单.
     *
     * @param context
     * @return
     */
    @Override
    public boolean match(OrderECouponSMSContext context) {
        return HuanleguUtil.SUPPLIER_DOMAIN_NAME.equals(context.getGoods().getSupplier().domainName);
    }
}

