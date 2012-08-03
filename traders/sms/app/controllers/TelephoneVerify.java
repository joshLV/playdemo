package controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sms.SMSUtil;
import models.supplier.SupplierStatus;
import org.apache.commons.codec.digest.DigestUtils;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;

/**
 * @author likang
 */

/**
 */
public class TelephoneVerify extends Controller {
    public static final String APP_KEY = Play.configuration.getProperty("tel_verify.app_key", "exos8BHw");

    /**
     * 电话验证
     *
     * @param caller    主叫号码
     * @param coupon    券号
     * @param timestamp 时间戳，UTC时间1970年1月1日零点至今的秒数，允许5分钟的上下浮动
     * @param sign      请求签名，由 分配的app_key+timestamp 拼接后进行MD5编码组成
     */
    public static void verify(String caller, String coupon, Long timestamp, String sign) {
        Logger.info("telephone verify; caller: %s; coupon: %s; timestamp: %s; sign: %s", caller, coupon, timestamp, sign);
        if (caller == null || caller.trim().equals("")) {
            Logger.info("telephone verify failed: invalid caller");
            renderText("1");//主叫号码无效
        }
        if (coupon == null || coupon.trim().equals("")) {
            Logger.info("telephone verify failed: invalid coupon");
            renderText("2");//券号无效
        }
        if (timestamp == null) {
            Logger.error("telephone verify failed: invalid timestamp; caller: %s; coupon: %s; timestamp: %s; sign: %s", caller, coupon, timestamp, sign);
            renderText("3");//时间戳无效
        }
        if (sign == null || sign.trim().equals("")) {
            Logger.error("telephone verify failed: invalid sign; caller: %s; coupon: %s; timestamp: %s; sign: %s", caller, coupon, timestamp, sign);
            renderText("4");//签名无效
        }

        //5分钟的浮动
        if (requestTimeout(timestamp, 300)) {
            Logger.error("telephone verify failed: request timeout %s; caller: %s; coupon: %s; timestamp: %s; sign: %s", caller, coupon, timestamp, sign);
            renderText("5");//请求超时
        }
        //验证密码
        if (!validSign(timestamp, sign)) {
            Logger.error("telephone verify failed: wrong sign; caller: %s; coupon: %s; timestamp: %s; sign: %s", caller, coupon, timestamp, sign);
            renderText("6");//签名错误
        }

        //查找店员
        SupplierUser supplierUser = SupplierUser.find("byLoginName", caller).first();
        if (supplierUser == null || supplierUser.shop == null
                || supplierUser.supplier == null
                || supplierUser.supplier.deleted == DeletedStatus.DELETED
                || supplierUser.supplier.status == SupplierStatus.FREEZE) {
            Logger.info("telephone verify failed: invalid caller %s", caller);
            renderText("8");//对不起，商户不存在
        }

        //开始验证
        ECoupon ecoupon = ECoupon.query(coupon, null);
        if (ecoupon == null) {
            Logger.info("telephone verify failed: coupon not found");
            renderText("7");//对不起，未找到此券
        }

        if (ecoupon.status == ECouponStatus.CONSUMED) {
            Logger.info("telephone verify failed: coupon consumed");
            renderText("10");//该券无法重复消费。消费时间为
        } else if (ecoupon.status != ECouponStatus.UNCONSUMED) {
            Logger.info("telephone verify failed: coupon status invalid. %s", ecoupon.status);
            renderText("11");//对不起，该券无法消费
        } else if (ecoupon.expireAt.before(new Date())) {
            Logger.info("telephone verify failed: coupon expired");
            renderText("12");//对不起，该券已过期
        } else {
            ecoupon.consumeAndPayCommission(supplierUser.shop.id, null, supplierUser, VerifyCouponType.CLERK_MESSAGE);
            String eCouponNumber = ecoupon.getMaskedEcouponSn();
            eCouponNumber = eCouponNumber.substring(eCouponNumber.lastIndexOf("*") + 1);

            String dateTime = DateUtil.getNowTime();

            // 发给消费者
            if (Play.mode.isProd()) {
                SMSUtil.send("【券市场】您尾号" + eCouponNumber + "的券号于" + dateTime
                        + "已成功消费，使用门店：" + supplierUser.shop.name + "。如有疑问请致电：400-6262-166", ecoupon.orderItems.phone, ecoupon.replyCode);
            }
            ecoupon.verifyType = VerifyCouponType.TELEPHONE;
            ecoupon.verifyTel = caller;
            ecoupon.save();

            Logger.info("telephone verify success; caller: %s; coupon: %s; timestamp: %s; sign: %s", caller, coupon, timestamp, sign);
            renderText("0");//消费成功，价值" + ecoupon.faceValue + "元
        }
    }

    /**
     * 查询面值
     *
     * @param coupon    券号
     * @param timestamp 时间戳，UTC时间1970年1月1日零点至今的秒数，允许5分钟的上下浮动
     * @param sign      请求签名，由 分配的app_key+timestamp 拼接后进行MD5编码组成
     */
    public static void faceValue(String coupon, Long timestamp, String sign) {
        Logger.info("query face value; coupon: %s; timestamp: %s; sign: %s", coupon, timestamp, sign);

        if (coupon == null || coupon.trim().equals("")) {
            Logger.info("query face value failed: invalid coupon");
            renderText("券号无效");//券号无效
        }

        ECoupon ecoupon = ECoupon.query(coupon, null);

        if (ecoupon == null) {
            Logger.info("query face value failed: coupon not found");
            renderText("此券不存在");
        }
        if (timestamp == null) {
            Logger.error("query face value failed: invalid timestamp; coupon: %s; timestamp: %s; sign: %s", coupon, timestamp, sign);
            renderText("时间戳无效");//时间戳无效
        }
        if (sign == null || sign.trim().equals("")) {
            Logger.error("query face value failed: invalid sign; coupon: %s; timestamp: %s; sign: %s", coupon, timestamp, sign);
            renderText("签名无效");//签名无效
        }

        //5分钟的浮动
        if (requestTimeout(timestamp, 300)) {
            Logger.error("query face value failed; coupon: %s; timestamp: %s; sign: %s", coupon, timestamp, sign);
            renderText("请求超时");//请求超时
        }
        //验证密码
        if (!validSign(timestamp, sign)) {
            Logger.error("query face value failed: wrong sign; coupon: %s; timestamp: %s; sign: %s", coupon, timestamp, sign);
            renderText("签名错误");
        }

        //只返回整数
        renderText("" + ecoupon.faceValue.intValue());
    }

    /**
     * 查询消费时间
     *
     * @param coupon    券号
     * @param timestamp 时间戳，UTC时间1970年1月1日零点至今的秒数，允许5分钟的上下浮动
     * @param sign      请求签名，由 分配的app_key+timestamp 拼接后进行MD5编码组成
     */
    public static void consumedAt(String coupon, Long timestamp, String sign) {
        Logger.info("query consumed at; coupon: %s; timestamp: %s; sign: %s", coupon, timestamp, sign);

        if (coupon == null || coupon.trim().equals("")) {
            Logger.info("query face value failed: invalid coupon");
            renderText("券号无效");//券号无效
        }

        //开始验证
        ECoupon ecoupon = ECoupon.query(coupon, null);

        if (ecoupon == null) {
            Logger.info("query face value failed: coupon not found");
            renderText("券号无效");
        }
        if (timestamp == null) {
            Logger.error("query face value failed: invalid timestamp; coupon: %s; timestamp: %s; sign: %s", coupon, timestamp, sign);
            renderText("时间戳无效");//时间戳无效
        }
        if (sign == null || sign.trim().equals("")) {
            Logger.error("query face value failed: invalid sign; coupon: %s; timestamp: %s; sign: %s", coupon, timestamp, sign);
            renderText("签名无效");//签名无效
        }

        //5分钟的浮动
        if (requestTimeout(timestamp, 300)) {
            Logger.error("query face value failed; coupon: %s; timestamp: %s; sign: %s", coupon, timestamp, sign);
            renderText("请求超时");//请求超时
        }
        //验证密码
        if (!validSign(timestamp, sign)) {
            Logger.error("query face value failed: wrong sign; coupon: %s; timestamp: %s; sign: %s", coupon, timestamp, sign);
            renderText("签名错误");
        }

        if (ecoupon.consumedAt == null) {
            Logger.info("telephone verify failed: coupon not consumed");
            renderText("未消费");
        }
        String shopName = "";
        if (ecoupon.shop == null) {
            Logger.info("telephone verify failed: coupon consumed, but do not know where it consumed at");
        } else {
            shopName = ",消费门店 " + ecoupon.shop.name;
        }
        renderText(new SimpleDateFormat("M月d日H点m分").format(ecoupon.consumedAt) + shopName);
    }

    private static boolean requestTimeout(Long timestamp, int seconds) {
        long t = System.currentTimeMillis() / 1000;
        return Math.abs(timestamp - t) > seconds;
    }

    private static boolean validSign(Long timestamp, String sign) {
        return DigestUtils.md5Hex(APP_KEY + timestamp).equals(sign);
    }
}
