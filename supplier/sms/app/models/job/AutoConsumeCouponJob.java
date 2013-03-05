package models.job;

import com.uhuila.common.constants.DeletedStatus;
import models.order.ECoupon;
import models.order.ECouponCreateType;
import play.jobs.Every;
import play.jobs.Job;
import util.extension.ExtensionResult;

import java.util.Date;
import java.util.List;

/**
 * @author likang
 *         Date: 12-12-25
 */
@Every("1mn")
public class AutoConsumeCouponJob extends Job {
    @Override
    public void doJob() {
        //2天内，5分钟前
        List<ECoupon> couponList = ECoupon.find("createdAt > ? and createdAt < ? and createType = ? and autoConsumed = ?",
                new Date(System.currentTimeMillis() - 2*24*60*60*1000), new Date(System.currentTimeMillis() - 5*60*1000),
                ECouponCreateType.IMPORT, DeletedStatus.UN_DELETED).fetch(20);
        for(ECoupon coupon : couponList) {
            ExtensionResult result = coupon.verifyAndCheckOnPartnerResaler();
            if (result.code == 0) { //验证成功
                coupon.autoConsumed = DeletedStatus.DELETED;
                coupon.save();
            }
        }
    }
}
