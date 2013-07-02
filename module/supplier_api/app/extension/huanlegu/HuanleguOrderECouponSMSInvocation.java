package extension.huanlegu;

import extension.order.OrderECouponSMSContext;
import extension.order.OrderECouponSMSInvocation;
import models.huanlegu.HuanleguMessage;
import models.huanlegu.HuanleguUtil;
import models.kangou.KangouCardStatus;
import models.kangou.KangouUtil;
import models.order.ECoupon;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.w3c.dom.Node;
import play.Logger;
import play.libs.XPath;
import util.extension.ExtensionResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: tanglq
 * Date: 13-4-19
 * Time: 下午2:50
 */
public class HuanleguOrderECouponSMSInvocation extends OrderECouponSMSInvocation {
    /**
     * 基于context的内容，生成短信内容，并通过context.setSmsContent()方法把短信内容传出.
     *
     * @param context
     * @return
     */
    @Override
    public ExtensionResult execute(OrderECouponSMSContext context) {
        Logger.info("HuanleguOrderECouponSMSInvocation(.id:");

        context.needSendSMS = false; //不发短信.

        Date now = new Date();
        for (ECoupon coupon : context.couponList) {
            //创建后10分钟内的不予重发
            if(DateUtils.addMinutes(coupon.createdAt, 10).after(now)){
                continue;
            }
            HuanleguMessage message = HuanleguUtil.resend(coupon);
            if (message.isResponseOk()){
                coupon.smsSentCount += 1;
                coupon.save();
            }
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
