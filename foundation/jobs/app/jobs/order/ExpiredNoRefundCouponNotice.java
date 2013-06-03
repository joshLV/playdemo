package jobs.order;

import com.uhuila.common.util.DateUtil;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.ktv.KtvRoomOrderInfo;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.OuterOrderPartner;
import models.sales.Goods;
import models.sales.Shop;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import play.Play;
import play.jobs.On;

import java.util.*;

/**
 * <p/>
 * User: yanjy
 * Date: 13-1-25
 * Time: 下午1:32
 */
@JobDefine(title = "虚拟验证券到期提醒", description = "查询三天后京东，WB未消费并且是不可退款的券，做虚拟虚证用")
@On("0 0 1 * * ?")
public class ExpiredNoRefundCouponNotice extends JobWithHistory {
    public static String MAIL_RECEIVER = Play.configuration.getProperty("mail.receiver", "dev@uhuila.com");

    @Override
    public void doJobWithHistory() {
        CouponsCondition condition = new CouponsCondition();
        condition.expiredAtBegin = DateUtils.addDays(new Date(), -3);
        condition.expiredAtEnd = DateUtils.addDays(new Date(), 3);
        List<ECoupon> resultList = ECoupon.findVirtualCoupons(condition);
        String subject = "虚拟验证券到期提醒";
        List<Map<String, String>> couponList = new ArrayList<>();
        Map<String, String> couponMap;


        List<Long> goodsIds = new ArrayList<>();
        for (ECoupon coupon : resultList) {
            if (!goodsIds.contains(coupon.goods.id) &&
                    (coupon.partner == ECouponPartner.JD || coupon.partner == ECouponPartner.WB)) {
                goodsIds.add(coupon.goods.id);
            }
        }
        int goodsCount = 0;
        for (Long goodsId : goodsIds) {
            couponMap = new HashMap<>();
            List<String> couponSnList = new ArrayList<>();
            for (ECoupon coupon : resultList) {
                if (coupon.partner != ECouponPartner.JD && coupon.partner != ECouponPartner.WB) {
                    continue;
                }

                if (!goodsId.equals(coupon.goods.id)) {
                    continue;
                }
                if (goodsCount == 0) {
                    couponMap.put("userId", String.valueOf(coupon.order.userId));
                    couponMap.put("partner", coupon.partner.toString());
                    //商品名称
                    couponMap.put("goodsName", coupon.goods.shortName);
                    couponMap.put("goodsId", goodsId.toString());
                }
                couponSnList.add(coupon.eCouponSn);
                goodsCount++;

            }
            couponMap.put("p_couponSn", StringUtils.join(couponSnList, ","));
            couponList.add(couponMap);
            goodsCount = 0;
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
