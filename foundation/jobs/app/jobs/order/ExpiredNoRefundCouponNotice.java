package jobs.order;

import com.uhuila.common.util.DateUtil;
import models.jobs.JobWithHistory;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.ECouponPartner;
import play.Play;
import play.jobs.On;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 13-1-25
 * Time: 下午1:32
 */
@On("0 0 1 * * ?")
public class ExpiredNoRefundCouponNotice extends JobWithHistory {
    public static String MAIL_RECEIVER = Play.configuration.getProperty("mail.receiver", "dev@uhuila.com");

    @Override
    public void doJobWithHistory() {
        CouponsCondition condition = new CouponsCondition();
        condition.expiredAtBegin = DateUtil.getBeginExpiredDate(3);
        condition.expiredAtEnd = DateUtil.getEndExpiredDate(3);
        List<ECoupon> resultList = ECoupon.findVirtualCoupons(condition);
        String subject = "券号到期提醒";
        List<Map<String, String>> couponList = new ArrayList<>();
        Map<String, String> couponMap;
        Long goodsId = null;
        String p_coupon = "";
        int i = 0;
        for (ECoupon coupon : resultList) {
            couponMap = new HashMap<>();
            if (coupon.partner == ECouponPartner.JD || coupon.partner == ECouponPartner.WB) {
                if (goodsId != null && goodsId.equals(coupon.goods.id)) {
                    p_coupon += "," + coupon.eCouponSn;
                    couponList.get(i - 1).put("p_couponSn", p_coupon);
                } else {
                    p_coupon = coupon.eCouponSn;
                    if (goodsId != null || i == 0) {
                        //分销商
                        couponMap.put("p_couponSn", p_coupon);
                        couponMap.put("couponSn", coupon.eCouponSn);
                        couponMap.put("userId", String.valueOf(coupon.order.userId));
                        couponMap.put("partner", ECouponPartner.JD.toString());
                        //商品名称
                        couponMap.put("goodsName", coupon.goods.shortName);
                        couponMap.put("goodsId", coupon.goods.id.toString());
                        couponList.add(couponMap);
                    }
                    goodsId = coupon.goods.id;
                }
                i++;
            }
        }
        if (couponList.size() > 0) {
            MailMessage mailMessage = new MailMessage();
            mailMessage.addRecipient(MAIL_RECEIVER);
            mailMessage.setSubject(Play.mode.isProd() ? subject : subject + "【测试】");
            mailMessage.putParam("subject", subject);
            mailMessage.putParam("couponList", couponList);
            MailUtil.sendExpiredNoRefundCouponMail(mailMessage);
        }
    }
}
