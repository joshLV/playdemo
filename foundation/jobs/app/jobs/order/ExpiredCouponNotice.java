package jobs.order;

import com.uhuila.common.util.DateUtil;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.OrderItemsFeeType;
import models.order.SentCouponMessage;
import models.sms.SMSMessage;
import play.Logger;

import javax.persistence.Query;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 每天12点发送短信提醒1周后过期的券
 *
 * @author 12-5-30
 *         Time: 下午1:57
 */
@JobDefine(title = "消费者券到期短信提醒", description = "每天12点向消费者发送短信提醒1周后过期的券")
// @ O n("0 0 12 * * ?")
public class ExpiredCouponNotice extends JobWithHistory {

    @Override
    public void doJobWithHistory() {
        String sql = "select e from ECoupon e where e.eCouponSn not in (select m.couponNumber from SentCouponMessage " +
                "m ) and e.isFreeze=0 and e.goods.isLottery=false and status =:status and (e.expireAt > :expireBeginAt and e.expireAt <= " +
                ":expireEndAt) order by e.id";
        Query query = ECoupon.em().createQuery(sql);
        query.setParameter("status", ECouponStatus.UNCONSUMED);
        query.setParameter("expireBeginAt", DateUtil.getBeginExpiredDate(6));
        query.setParameter("expireEndAt", DateUtil.getEndExpiredDate(6));
        query.setFirstResult(0);
        query.setMaxResults(200);
        List<ECoupon> expiredCoupons = query.getResultList();
        Logger.info("expiredCoupons.size=" + expiredCoupons.size());

        String pre_phone = "";
        String pre_goodsName = "";
        String mobile = "";
        String goodsName = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (ECoupon coupon : expiredCoupons) {
            if (coupon.order.isWebsiteOrder()) {
                Logger.info("order.id:" + coupon.order.id + " 是一百券订单，不需要处理");
                // 一百券网站订单不需要处理
                break;
            }
            mobile = coupon.orderItems.phone;
            goodsName = coupon.goods.name;
            Logger.info("mobile=" + mobile + ", goodsName=" + goodsName);
            if (!pre_phone.equals(mobile) || !pre_goodsName.equals(goodsName)) {
                new SMSMessage("您的" + goodsName + "，将要过期，请注意消费截止日期为" + sdf.format(coupon.expireAt) + "。",
                        mobile,
                        coupon.replyCode)
                        .orderItemsId(coupon.orderItems.id)
                        .feeType(OrderItemsFeeType.SMS_EXPIRE_NOTIFY)
                        .send();
                pre_goodsName = goodsName;
                pre_phone = mobile;
            }
            new SentCouponMessage(coupon.eCouponSn, mobile, coupon.goods.name).save();
        }
    }
}

