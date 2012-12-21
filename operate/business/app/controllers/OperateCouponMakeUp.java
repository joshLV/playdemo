package controllers;

import models.order.ECoupon;
import models.order.ECouponStatus;
import models.taobao.TaobaoCouponUtil;
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
            renderText("输入partner和coupon"); return;
        }

        if (partner.equalsIgnoreCase("taobao")) {
            ECoupon eCoupon = ECoupon.find("byECouponSn", coupon).first();
            if (eCoupon == null) {
                renderText("coupon没找到"); return;
            }
            if (eCoupon.status != ECouponStatus.CONSUMED) {
                renderText("coupon的消费状态不是已消费"); return;
            }
            if( TaobaoCouponUtil.verifyOnTaobao(eCoupon)){
                renderText("id" + eCoupon.id + "在淘宝消费成功");return;
            }else {
                renderText("id" + eCoupon.id + "在淘宝消费失败");return;
            }
        }
    }
}
