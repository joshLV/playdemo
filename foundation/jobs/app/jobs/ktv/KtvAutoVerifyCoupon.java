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
@JobDefine(title = "自动验证KTV因预订时间过期而导致无效的券", description = "每天10分钟执行，自动验证KTV因预订时间过期而导致无效的券")
@Every("10mn")
public class KtvAutoVerifyCoupon extends JobWithHistory {
    @Override
    public void doJobWithHistory() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        Date today = DateUtils.truncate(new Date(), Calendar.DATE);

        //查找3天内，过期时间小于当前时间的券
        Query query = JPA.em().createQuery("select k from KtvRoomOrderInfo k where k.status = :status and " +
                "(" +
                "   ( k.scheduledDay = :today and (k.scheduledTime + k.product.duration) <= :hour) " +
                "   or " +
                "   ( k.scheduledDay >= :threeDaysAgo and k.scheduledDay < :today and ( k.scheduledTime + k.product.duration - 24) <= :hour ) " +
                ") order by k.id ");
        query.setParameter("threeDaysAgo", DateUtils.addDays(today, -3));
        query.setParameter("status", KtvOrderStatus.DEAL);
        query.setParameter("today", today);
        query.setParameter("hour", hour);


        List<KtvRoomOrderInfo> roomOrderInfoList = query.getResultList();

        for (KtvRoomOrderInfo roomOrderInfo : roomOrderInfoList) {
            List<ECoupon> couponList = ECoupon.find("orderItems = ? and status = ? ",
                    roomOrderInfo.orderItem, ECouponStatus.UNCONSUMED).fetch();

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
