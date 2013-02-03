package models.job;

import com.uhuila.common.constants.DeletedStatus;
import models.dangdang.groupbuy.DDGroupBuyUtil;
import models.jingdong.groupbuy.JDGroupBuyUtil;
import models.order.ECoupon;
import models.order.ECouponCreateType;
import models.order.ECouponPartner;
import models.taobao.TaobaoCouponUtil;
import models.wuba.WubaUtil;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

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
            boolean consumed = true;
            if (coupon.partner == ECouponPartner.JD) {
                if (!JDGroupBuyUtil.verifyOnJingdong(coupon)) {
                    consumed = false;
                    Logger.info("verify on jingdong failed");
                }
            } else if (coupon.partner == ECouponPartner.WB) {
                if (!WubaUtil.verifyOnWuba(coupon)) {
                    consumed = false;
                    Logger.info("verify on wuba failed");
                }
            } else if (coupon.partner == ECouponPartner.TB) {
                if (!TaobaoCouponUtil.verifyOnTaobao(coupon)) {
                    consumed = false;
                    Logger.info("verify on taobao failed");
                }
            } else if (coupon.partner == ECouponPartner.DD) {
                if (!DDGroupBuyUtil.verifyOnDangdang(coupon)) {
                    consumed = false;
                    Logger.info("verify on dangdang failed");
                }
            }else {
                consumed = false;
            }
            if (consumed) {
                coupon.autoConsumed = DeletedStatus.DELETED;
                coupon.save();
            }
        }
    }
}
