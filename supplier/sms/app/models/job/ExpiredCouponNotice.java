package models.job;

import com.uhuila.common.util.DateUtil;
import models.accounts.AccountType;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.SentCouponMessage;
import models.sms.SMSUtil;
import play.jobs.Job;

import javax.persistence.Query;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

/**
 * 每天12点发送短信提醒1周后过期的券
 *
 * @author 12-5-30
 *         Time: 下午1:57
 */
// @ O n("0 0 12 * * ?")
public class ExpiredCouponNotice extends Job {

    @Override
    public void doJob() throws Exception {
        String sql = "select e from ECoupon e where e.eCouponSn not in (select m.couponNumber from SentCouponMessage " +
                "m ) and e.isFreeze=0 and e.goods.isLottery=false and status =:status and (e.expireAt > :expireBeginAt and e.expireAt <= " +
                ":expireEndAt) order by e.id";
        Query query = ECoupon.em().createQuery(sql);
        query.setParameter("status", ECouponStatus.UNCONSUMED);
        query.setParameter("expireBeginAt", DateUtil.getBeginExpiredDate(6));
        query.setParameter("expireEndAt", DateUtil.getEndExpiredDate(7));
        query.setFirstResult(0);
        query.setMaxResults(200);
        ECoupon coupon = null;
        List<ECoupon> expiredCoupons = query.getResultList();
        Iterator<ECoupon> it = expiredCoupons.iterator();

        String pre_phone = "";
        String pre_goodsName = "";
        String mobile = "";
        String goodsName = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        while (it.hasNext()) {
            coupon = it.next();
            if (coupon.order.userType == AccountType.CONSUMER) {
                mobile = coupon.orderItems.phone;
                goodsName = coupon.goods.name;
                if (!pre_phone.equals(mobile) || !pre_goodsName.equals(goodsName)) {
                    SMSUtil.send("【一百券】您的" + goodsName + "，将要过期，请注意消费截止日期为" + sdf.format(coupon.expireAt) + "。",
                            mobile,
                            coupon.replyCode);
                    pre_goodsName = goodsName;
                    pre_phone = mobile;
                }
                new SentCouponMessage(coupon.eCouponSn, mobile, coupon.goods.name).save();
            }
        }
    }

}

