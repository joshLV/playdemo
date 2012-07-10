package controllers;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.Shop;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
import org.apache.commons.codec.digest.DigestUtils;
import play.Logger;
import play.Play;
import play.mvc.Controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author likang
 */

/**
 */
public class TelephoneVerify extends Controller{
    public static final String APP_KEY = Play.configuration.getProperty("tel_verify.app_key", "exos8BHw");

    /**
     * 电话验证
     *
     * @param caller    主叫号码
     * @param coupon    券号
     * @param timestamp 时间戳，UTC时间1970年1月1日零点至今的秒数，允许5分钟的上下浮动
     * @param sign      请求签名，由 分配的app_key+timestamp 拼接后进行MD5编码组成
     */
    public static void verify(String caller, String coupon, Long timestamp, String sign){
        Logger.info("telephone verify; caller: %s; coupon: %s; timestamp: %s; sign: %s", caller, coupon, timestamp, sign);
        if(caller == null || caller.trim().equals("")){
            Logger.error("telephone verify failed: invalid caller");
            renderText("1");//主叫号码无效
        }
        if(coupon == null || coupon.trim().equals("")){
            Logger.error("telephone verify failed: invalid coupon");
            renderText("2");//券号无效
        }
        if(timestamp == null){
            Logger.error("telephone verify failed: invalid timestamp");
            renderText("3");//时间戳无效
        }
        if(sign == null || sign.trim().equals("")){
            Logger.error("telephone verify failed: invalid sign");
            renderText("4");//签名无效
        }

        //5分钟的浮动
        if(requestTimeout(timestamp, 300)){
            Logger.error("telephone verify failed: request timeout %s", timestamp);
            renderText("5");//请求超时
        }
        //验证密码
        if(!validSign(timestamp, sign)){
            Logger.error("telephone verify failed: wrong sign");
            renderText("6");//签名错误
        }

        //开始验证
        ECoupon ecoupon = ECoupon.query(coupon, null);

        if (ecoupon == null) {
            Logger.error("telephone verify failed: coupon not found");
            renderText("7");//对不起，未找到此券
        }

        Long supplierId = ecoupon.goods.supplierId;

        Supplier supplier = Supplier.findById(supplierId);

        if (supplier == null || supplier.deleted == DeletedStatus.DELETED || supplier.status == SupplierStatus.FREEZE) {
            Logger.error("telephone verify failed: invalid supplier %s",supplierId);
            renderText("8");//对不起，商户不存在
        }
        SupplierUser supplierUser = SupplierUser.find("from SupplierUser where supplier.id=? and loginName=?", supplierId, caller).first();
        if(supplierUser == null){
            Logger.error("telephone verify failed: supplier user not found %s %s",supplierId, caller);
            renderText("9");//对不起，未找到该店员
        }

        Long shopId = supplierUser.shop.id;
        Shop shop = Shop.findById(shopId);
        String shopName = shop.name;

        if (ecoupon.status == ECouponStatus.CONSUMED) {
            Logger.error("telephone verify failed: coupon consumed");
            renderText("10");//该券无法重复消费。消费时间为
        } else if (ecoupon.status != ECouponStatus.UNCONSUMED){
            Logger.error("telephone verify failed: coupon status invalid. %s", ecoupon.status);
            renderText("11");//对不起，该券无法消费
        } else if (ecoupon.expireAt.before(new Date())) {
            Logger.error("telephone verify failed: coupon expired");
            renderText("12");//对不起，该券已过期
        }  else {
            ecoupon.consumeAndPayCommission(supplierUser.shop.id, supplierUser, VerifyCouponType.CLERK_MESSAGE);
            String eCouponNumber = ecoupon.getMaskedEcouponSn();
            eCouponNumber = eCouponNumber.substring(eCouponNumber.lastIndexOf("*") + 1);

            String dateTime = DateUtil.getNowTime();

            // 发给消费者
            if(Play.mode.isProd()){
                SMSUtil.send("【券市场】您尾号" + eCouponNumber + "的券号于" + dateTime
                        + "已成功消费，使用门店：" + shopName + "。如有疑问请致电：400-6262-166", ecoupon.orderItems.phone, ecoupon.replyCode);
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
    public static void faceValue(String coupon, Long timestamp, String sign){
        Logger.info("query face value; coupon: %s; timestamp: %s; sign: %s", coupon, timestamp, sign);

        ECoupon ecoupon = ECoupon.query(coupon, null);

        if (ecoupon == null) {
            Logger.error("query face value failed: coupon not found");
            renderText("此券不存在");
        }
        if(timestamp == null){
            Logger.error("query face value failed: invalid timestamp");
            renderText("时间戳无效");//时间戳无效
        }
        if(sign == null || sign.trim().equals("")){
            Logger.error("query face value failed: invalid sign");
            renderText("签名无效");//签名无效
        }

        //5分钟的浮动
        if(requestTimeout(timestamp, 300)){
            Logger.error("query face value failed: request timeout %s", timestamp);
            renderText("请求超时");//请求超时
        }
        //验证密码
        if(!validSign(timestamp, sign)){
            Logger.error("query face value failed: wrong sign");
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
    public static void consumedAt(String coupon, Long timestamp, String sign){
        //开始验证
        ECoupon ecoupon = ECoupon.query(coupon, null);

        if (ecoupon == null) {
            Logger.error("query face value failed: coupon not found");
            renderText("此券不存在");
        }
        if(timestamp == null){
            Logger.error("query face value failed: invalid timestamp");
            renderText("时间戳无效");//时间戳无效
        }
        if(sign == null || sign.trim().equals("")){
            Logger.error("query face value failed: invalid sign");
            renderText("签名无效");//签名无效
        }

        //5分钟的浮动
        if(requestTimeout(timestamp, 300)){
            Logger.error("query face value failed: request timeout %s", timestamp);
            renderText("请求超时");//请求超时
        }
        //验证密码
        if(!validSign(timestamp, sign)){
            Logger.error("query face value failed: wrong sign");
            renderText("签名错误");
        }

        if (ecoupon.consumedAt == null){
            Logger.error("telephone verify failed: coupon not consumed");
            renderText("未消费");
        }else {
            renderText(new SimpleDateFormat("yyyy年M月d日h点m分").format(ecoupon.consumedAt));
        }
    }

    private static boolean requestTimeout(Long timestamp, int seconds){
        long t = System.currentTimeMillis()/1000;
        return Math.abs(timestamp - t) > seconds;
    }

    private static boolean validSign(Long timestamp, String sign){
        return DigestUtils.md5Hex(APP_KEY + timestamp).equals(sign);
    }
}
