package models.job.resale;

import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.ECouponStatus;
import models.sina.SinaVouchersMessageUtil;
import play.jobs.Every;

import java.util.List;

/**
 * User: yan
 * Date: 13-3-26
 * Time: 上午11:56
 */
@JobDefine(title="微博钱包券同步")
//@Every("1mn")
public class SinaECouponScanner extends JobWithHistory {
    @Override
    public void doJobWithHistory() {
        List<ECoupon> couponList = ECoupon.find("synced = false and isFreeze = 0 and status = ? and partner=?",
                ECouponStatus.UNCONSUMED, ECouponPartner.SINA).fetch();
        for (ECoupon coupon : couponList) {
            SinaVouchersMessageUtil.send(coupon.id);
        }
    }
}
