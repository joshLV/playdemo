package controllers.thirdtuan;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.Goods;
import models.sales.Shop;
import models.sms.SMSUtil;
import models.tsingtuan.TsingTuanOrder;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.mvc.Controller;

import com.uhuila.common.util.DateUtil;

public class TsingTuans extends Controller {

    /**
     * 
        券接收成功回调
        http://api.quanfx.com/api/v1/tsingtuan/coupons
        参数名 类型 描述 备注
        coupon 优惠券 10位数字
        team_id Int 为清团项目编号
        sign 签名 为了保证此接口不被恶意使用，需要提供加密保护，生成规则为：md5(coupon| team_id|secret);
        其中secret 为密钥，为我们约定的一样
        
        返回格式： <return_code>|<Message>
        包括：
        0|成功
        1|此券不存在
     * @param team_id
     */
    public static void coupons(String coupon, String team_id, String check_time, String sign) {
        
        System.out.println("coupon=" + coupon + ",team_id=" + team_id 
                        + ", check_time=" + check_time + ", sign=" + sign);

        if (StringUtils.isBlank(coupon) || StringUtils.isBlank(team_id) || StringUtils.isBlank(sign)) {
            renderText("9|参数非法");
        }

        System.out.println("md5=" + md5sum(coupon, team_id, check_time, TsingTuanOrder.SECRET));
        if (!sign.equals(md5sum(coupon, team_id, check_time, TsingTuanOrder.SECRET))) {
            renderText("8|参数MD5校验失败");
        }
        
        ECoupon ecoupon = ECoupon.find("eCouponSn=?", coupon).first();
        if (ecoupon == null) {
            renderText("1|券不存在");
        }
        TsingTuanOrder tsingTuanOrder = TsingTuanOrder.from(ecoupon);
        if (tsingTuanOrder == null) {
            renderText("1|券不存在");
        }
        ecoupon.synced = Boolean.TRUE;
        ecoupon.save();
        
        renderText("0|成功");
    }
    
    
    /**
     * http://api.quanfx.com/api/v1/tsingtuan/check
            参数名 类型 描述 备注
            coupon 优惠券 10位数字
            team_id Int 为清团项目编号
            check_time Int 验证时间
            sign 签名 为了保证此接口不被恶意使用，需要提供加密保护，生成规则为：md5(coupon| team_id| check_time|secret);
            其中secret 为密钥，为我们约定的一样
            
            返回格式： <return_code>|<Message>
            包括：
            1|已退款不能消费
            2|已验证不能重复验证
            3|已过有效期
     */
    public static void check(String coupon, String team_id, String check_time, String sign) {
//        System.out.println("check. coupon=" + coupon + ",team_id=" + team_id
//                        + ", check_time=" + check_time + ", sign=" + sign);

        if (StringUtils.isBlank(coupon) || StringUtils.isBlank(team_id) || StringUtils.isBlank(check_time) || StringUtils.isBlank(sign)) {
            renderText("9|参数非法");
        }
//        System.out.println("md5=" + md5sum(coupon, team_id, check_time, TsingTuanOrder.SECRET));
        if (!sign.equals(md5sum(coupon, team_id, check_time, TsingTuanOrder.SECRET))) {
            renderText("8|参数MD5校验失败");
        }

        ECoupon ecoupon = ECoupon.find("eCouponSn=?", coupon).first();
        if (ecoupon == null) {
            renderText("1|券不存在");
        }
        TsingTuanOrder tsingTuanOrder = TsingTuanOrder.from(ecoupon);
        if (tsingTuanOrder == null) {
            renderText("1|券不存在");
        }
        
        if (ecoupon.expireAt.before(new Date())) {
            //过期
            sendSmsToConsumer("【清团】您的券号" + coupon + "已过期，无法进行消费。如有疑问请致电：4006865151", ecoupon.orderItems.phone, ecoupon.replyCode);
            renderText("3|已过有效期");
        } else if (ecoupon.status == ECouponStatus.UNCONSUMED) {
            String couponLastCode = ecoupon.getLastCode(4);
            String dateTime = DateUtil.getNowTime();

            Goods goods = ecoupon.goods;
            
            Collection<Shop> shops = goods.getShopList();
            if (shops == null || shops.size() == 0) {
                Logger.error("goods.id=" + goods.id + " NOT assign any SHOP!!");
                renderText("10|未知错误");
            }
            Long shopId = null;
            for (Shop shop : shops) {
                shopId = shop.id;
            }
            if(!ecoupon.consumeAndPayCommission(shopId, null, VerifyCouponType.CONSUMER_MESSAGE)){
                renderText("1|已退款不能消费");
            }
            
            // 发给消费者
            sendSmsToConsumer("您尾号" + couponLastCode + "券于" + dateTime
                    + "成功消费。客服4006865151", ecoupon.orderItems.phone, ecoupon.replyCode);
            renderText("0|成功");
        } else if (ecoupon.status == ECouponStatus.CONSUMED) {
            String couponLastCode = ecoupon.getLastCode(4);
            SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm");
            // 发给消费者
            sendSmsToConsumer("您尾号" + couponLastCode + "券不能重复消费，已于" + df.format(ecoupon.consumedAt)
                    + "消费过", ecoupon.orderItems.phone, ecoupon.replyCode);

            renderText("2|已验证不能重复验证");
        }
        renderText("10|未知错误");
    }
    
    private static String md5sum(String... args) {
        String value = StringUtils.join(args, "|");
        return DigestUtils.md5Hex(value);
    }

    private static void sendSmsToConsumer(String message, String mobile, String code) {
        SMSUtil.send(message, mobile, code);
    }
}
