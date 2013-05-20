package jobs.order;

import com.uhuila.common.constants.DeletedStatus;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.order.ECoupon;
import models.order.ECouponCreateType;
import play.Logger;
import play.jobs.Every;
import util.extension.ExtensionResult;

import java.util.Date;
import java.util.List;

/**
 * @author likang
 *         Date: 12-12-25
 */
@JobDefine(title = "自动验证券", description = "导入券在售出后，需要自动验证")
@Every("1mn")
public class AutoConsumeCouponJob extends JobWithHistory {
    @Override
    public void doJobWithHistory() {
        //2天内，5分钟前
        List<ECoupon> couponList = ECoupon.find("createdAt > ? and createdAt < ? and createType = ? and autoConsumed = ? order by id desc",
                new Date(System.currentTimeMillis() - 20*24*60*60*1000), new Date(System.currentTimeMillis() - 5*60*1000),
                ECouponCreateType.IMPORT, DeletedStatus.UN_DELETED).fetch(20);
        for(ECoupon coupon : couponList) {
            Logger.info("AutoConsumeCouponJob.verify eCouponSN=%s", coupon.eCouponSn);
            ExtensionResult result = coupon.verifyAndCheckOnPartnerResaler();
            Logger.info("   AutoConsumeCouponJob.verifyAndCheckOnPartnerResaler eCouponSN:" + coupon.eCouponSn + " result:" + result);
            if (result.isOk()) { //验证成功
                coupon.autoConsumed = DeletedStatus.DELETED;
                coupon.save();
            }
        }
    }
}
