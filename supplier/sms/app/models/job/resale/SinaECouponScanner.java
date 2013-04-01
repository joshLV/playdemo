package models.job.resale;

import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.ECouponStatus;
import models.sina.SinaVouchersMessageUtil;
import play.jobs.Job;

import java.util.List;

/**
 * User: yan
 * Date: 13-3-26
 * Time: 上午11:56
 */
//@Every("1mn")
public class SinaECouponScanner extends Job {
    @Override
    public void doJob() {
        List<ECoupon> couponList = ECoupon.find("synced = false and isFreeze = 0 and status = ? and partner=?",
                ECouponStatus.UNCONSUMED, ECouponPartner.SINA).fetch();
        for (ECoupon coupon : couponList) {
            SinaVouchersMessageUtil.send(coupon.id);
        }
    }
}
