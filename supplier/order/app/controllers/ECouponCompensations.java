package controllers;

import models.order.ECoupon;
import models.order.ECouponCompensation;
import models.order.ECouponStatus;
import play.Logger;
import play.mvc.Controller;
import util.extension.ExtensionResult;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 用于补偿没有发出来的第三方验证请求。
 *
 * @author tanglq
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
                ExtensionResult result = ecoupon.verifyAndCheckOnPartnerResaler();
                if (result.code == 0) {
                    ec.compensatedAt = new Date();
                    ec.result = ecoupon.partner + " verify SUCCESS";
                    count += 1;
                    amount = amount.add(ecoupon.salePrice);
                } else {
                    ec.result = "Failed verfion " + ecoupon.partner + " at " + new Date() + ":" + result;
                    Logger.info("Compensate verify on jingdong failed");
                }
                ec.save();
            }
        }
        renderArgs.put("count", count);
        renderArgs.put("amount", amount);
        renderText("Found " + compensations.size() + " ecoupon_compensations, compensated " + count + " record, amount: ￥" + amount);
    }
}
