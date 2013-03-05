package controllers;

import models.order.ECoupon;
import models.order.ECouponStatus;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;
import play.mvc.With;
import util.extension.ExtensionResult;

/**
 * @author likang
 *         Date: 12-12-21
 */

@With(OperateRbac.class)
public class OperateCouponMakeUp extends Controller {
    public static void index(String partner, String coupon) {
        if (StringUtils.isBlank(partner) || StringUtils.isBlank(coupon)) {
            renderText("请输入partner和coupon,多个coupon请用半角逗号分割"); return;
        }

        String[] couponList = coupon.trim().split(",");
        StringBuilder successMessage = new StringBuilder("成功：");
        StringBuilder failMessage = new StringBuilder("失败：\n");
        for(String c : couponList) {
            ECoupon eCoupon = ECoupon.find("byECouponSn", c).first();
            if (eCoupon == null) {
                failMessage.append(c).append(" 没有找到\n");
                continue;
            }
            if (eCoupon.status != ECouponStatus.CONSUMED) {
                failMessage.append(c).append(" 状态不是已消费\n");
                continue;
            }

            ExtensionResult result = eCoupon.verifyAndCheckOnPartnerResaler();

            if (result.isOk()) {
                successMessage.append(c).append(",");
            }else {
                failMessage.append(c).append(" 在第三方消费失败-").append(result.toString()).append("\n");
            }
        }

        renderText("输入：" + coupon + "\n"
                + successMessage + "\n"
                + failMessage);
    }
}
