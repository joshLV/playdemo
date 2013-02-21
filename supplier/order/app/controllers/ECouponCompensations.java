package controllers;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import models.jingdong.groupbuy.JDGroupBuyHelper;
import models.jingdong.groupbuy.JDGroupBuyUtil;
import models.order.ECoupon;
import models.order.ECouponCompensation;
import models.order.ECouponPartner;
import models.order.ECouponStatus;
import play.Logger;
import play.mvc.Controller;

/**
 * 用于补偿没有发出来的第三方验证请求。
 * 
 * @author tanglq
 * 
 */
public class ECouponCompensations extends Controller {

    public static final String SECRET = "5d41402abc4b2a76b9719d911017c592";

    public static void consumed(String secret) {
        if (!SECRET.equals(secret)) {
            error("Need secret!");
        }

        BigDecimal amount = BigDecimal.ZERO;
        Integer count = 0;
        List<ECouponCompensation> compensations = ECouponCompensation
                        .findTodoCompensations(ECouponCompensation.CONSUMED);
        for (ECouponCompensation ec : compensations) {
            ECoupon ecoupon = ec.ecpuon;

            // 只有已消费的才需要补偿
            if (ecoupon.status == ECouponStatus.CONSUMED) {
                if (ecoupon.partner == ECouponPartner.JD) {
                    if (JDGroupBuyHelper.verifyOnJingdong(ecoupon)) {
                        ec.compensatedAt = new Date();
                        ec.result = "360Buy.com verify SUCCESS";
                        count += 1;
                        amount = amount.add(ecoupon.salePrice);
                    } else {
                        ec.result = "Failed verfion 360buy at " + new Date();
                        Logger.info("Compensate verify on jingdong failed");
                    }
                    ec.save();
                }
            }
        }
        renderArgs.put("count", count);
        renderArgs.put("amount", amount);
        renderText("Found " + compensations.size() + " ecoupon_compensations, compensated " + count + " record, amount: ￥" + amount);
    }
}
