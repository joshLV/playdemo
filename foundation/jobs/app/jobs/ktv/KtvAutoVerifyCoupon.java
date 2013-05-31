package jobs.ktv;

import com.uhuila.common.constants.DeletedStatus;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.ktv.KtvOrderStatus;
import models.ktv.KtvRoomOrderInfo;
import models.order.ECoupon;
import models.order.ECouponHistoryMessage;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import org.apache.commons.lang.time.DateUtils;
import play.db.jpa.JPA;
import play.jobs.Every;
import play.jobs.On;

import javax.persistence.Query;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: yan
 * Date: 13-5-29
 * Time: 下午3:37
 */
@JobDefine(title = "自动验证KTV当天时间范围内未消费的过期券", description = "每天10分钟执行，把KTV当天时间范围内未消费的过期券自动验证掉")
@Every("10mn")
public class KtvAutoVerifyCoupon extends JobWithHistory {
    @Override
    public void doJobWithHistory() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        List<KtvRoomOrderInfo> roomOrderInfoList = KtvRoomOrderInfo.find("select k from KtvRoomOrderInfo k where " +
                "k.scheduledDay = ? and k.status = ? and (k.scheduledTime + k.product.duration) <= ? order by k.id",
                DateUtils.truncate(new Date(), Calendar.DATE), KtvOrderStatus.DEAL, hour).fetch();
        for (KtvRoomOrderInfo roomOrderInfo : roomOrderInfoList) {
            List<ECoupon> couponList = ECoupon.find("orderItems = ? and status = ? and expireAt >= ?",
                    roomOrderInfo.orderItem, ECouponStatus.UNCONSUMED, roomOrderInfo.scheduledDay).fetch();

            for (ECoupon coupon : couponList) {
                coupon.verifyType = VerifyCouponType.AUTO_VERIFY;
                coupon.status = ECouponStatus.CONSUMED;
                coupon.consumedAt = new Date();
                coupon.save();
                ECouponHistoryMessage.with(coupon).remark("KTV未消费的过期券自动验证")
                        .fromStatus(ECouponStatus.UNCONSUMED).toStatus(ECouponStatus.CONSUMED).sendToMQ();
            }
        }

    }
}
