package controllers;

import models.dangdang.groupbuy.DDGroupBuyUtil;
import models.jingdong.groupbuy.JDGroupBuyUtil;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.taobao.TaobaoCouponUtil;
import models.wuba.WubaUtil;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;
import play.mvc.With;

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

            boolean success = false;
            if (partner.equalsIgnoreCase("taobao"))
                success = TaobaoCouponUtil.verifyOnTaobao(eCoupon);
            else if (partner.equalsIgnoreCase("jingdong"))
                success = JDGroupBuyUtil.verifyOnJingdong(eCoupon);
            else if (partner.equalsIgnoreCase("wuba"))
                success = WubaUtil.verifyOnWuba(eCoupon);
            else if (partner.equalsIgnoreCase("dangdang")){
                success = DDGroupBuyUtil.verifyOnDangdang(eCoupon);
            }
            if (success) {
                successMessage.append(c).append(",");
            }else {
                failMessage.append(c).append(" 在第三方消费失败\n");
            }
        }

        renderText("输入：" + coupon + "\n"
                + successMessage + "\n"
                + failMessage);
    }
}
