package models.job;

import com.uhuila.common.util.DateUtil;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.ECouponStatus;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;

import javax.persistence.Query;
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
//@On("0 0 1 * * ?")
@Every("1mn")
public class ExpiredNoRefundCouponNotice extends Job {
    public static String MAIL_RECEIVER = Play.configuration.getProperty("mail.receiver", "yanjingyun@uhuila.com");

    @Override
    public void doJob() {
        String sql = "select e from ECoupon e where e.eCouponSn not in (select m.couponNumber from SentCouponMessage " +
                "m ) and e.isFreeze=0  and e.goods.noRefund= true and e.goods.isLottery=false and status =:status " +
                "and (e.expireAt >= :expireBeginAt and e.expireAt <= " +
                ":expireEndAt) and e.partner in (:partner) order by e.id";
        List<ECouponPartner> partnerList = new ArrayList<>();
        partnerList.add(ECouponPartner.JD);
        partnerList.add(ECouponPartner.WB);
        Query query = ECoupon.em().createQuery(sql);
        query.setParameter("status", ECouponStatus.UNCONSUMED);
        query.setParameter("expireBeginAt", DateUtil.getBeginExpiredDate(3));
        query.setParameter("expireEndAt", DateUtil.getEndExpiredDate(3));
        query.setParameter("partner", partnerList);
        query.setFirstResult(0);
        query.setMaxResults(200);
        List<ECoupon> resultList = query.getResultList();
        String subject = "券号到期提醒";
        List<Map<String, String>> couponList = new ArrayList<>();
        Map<String, String> couponMap;

        List<String> jdCoupons = new ArrayList();
        List<String> wuCoupons = new ArrayList();

        Long goodsId = null;
        String p_coupon = "";
        Map<String, String> partnerMap = new HashMap<>();
        for (ECoupon coupon : resultList) {
            couponMap = new HashMap<>();
            if (coupon.partner == ECouponPartner.JD) {
                jdCoupons.add(coupon.eCouponSn);
            }
            if (coupon.partner == ECouponPartner.WB) {
                wuCoupons.add(coupon.eCouponSn);
            }

            //分销商
            couponMap.put("partner", coupon.partner.toString());
            couponMap.put("couponSn", coupon.eCouponSn);

            if (goodsId == coupon.goods.id) {
                p_coupon += coupon.eCouponSn + ",";
            } else {
                p_coupon = coupon.eCouponSn;
            }

            couponMap.put("p_couponSn", p_coupon);
            if (goodsId != coupon.goods.id) {
                goodsId = coupon.goods.id;
                //商品名称
                couponMap.put("goodsName", coupon.goods.shortName);
                couponList.add(couponMap);
            }
        }

        partnerMap.put(ECouponPartner.JD.toString(), StringUtils.join(jdCoupons, ","));
        partnerMap.put(ECouponPartner.WB.toString(), StringUtils.join(wuCoupons, ","));
//        for (Map<String, String> map : couponList) {
//            System.out.println(map.get(map.get("couponSn") + "goodsName") + ">>>>map.get" + map.get("couponSn"));
//            System.out.println(partnerMap.get(map.get("couponSn") + map.get("partner") + ">>>>map.get"));
//
//        }
        if (couponList.size() > 0) {
            MailMessage mailMessage = new MailMessage();
            mailMessage.addRecipient(MAIL_RECEIVER);
            mailMessage.setSubject(Play.mode.isProd() ? subject : subject + "【测试】");
            mailMessage.putParam("subject", subject);
            mailMessage.putParam("partnerMap", partnerMap);
            mailMessage.putParam("couponList", couponList);
            MailUtil.sendExpiredNoRefundCouponMail(mailMessage);
        }
    }
}
