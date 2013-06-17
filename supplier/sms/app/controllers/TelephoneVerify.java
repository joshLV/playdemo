package controllers;

import com.google.gson.Gson;
import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.RmbUtil;
import models.admin.SupplierUser;
import models.admin.SupplierUserType;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.OrderItemsFee;
import models.order.OrderItemsFeeType;
import models.order.VerifyCouponType;
import models.sms.SMSMessage;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import util.transaction.RemoteRecallCheck;
import util.transaction.TransactionCallback;
import util.transaction.TransactionRetry;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author likang
 */

/**
 */
public class TelephoneVerify extends Controller {
    public static final String APP_KEY = Play.configuration.getProperty("tel_verify.app_key", "exos8BHw");
    public static final String COUPON_DATE = "yyyy-MM-dd HH:mm:ss";


    /**
     * 电话验证
     *
     * @param caller    主叫号码
     * @param coupon    券号
     * @param timestamp 时间戳，UTC时间1970年1月1日零点至今的秒数，允许5分钟的上下浮动
     * @param sign      请求签名，由 分配的app_key+timestamp 拼接后进行MD5编码组成
     */
    public static void verify2(final String caller, final String coupon, Long timestamp, String sign,
                               BigDecimal value) {
        Logger.info("telephone verify start: caller phone=%s, coupon=%s", caller, coupon);
        Logger.info(new Gson().toJson(request.params.allSimple()));
        if (caller == null || caller.trim().equals("")) {
            Logger.info("telephone verify failed: invalid caller");
            renderText("1|主叫号码无效");//主叫号码无效 1
        }
        if (coupon == null || coupon.trim().equals("")) {
            Logger.info("telephone verify failed: invalid coupon");
            renderText("2|券号为空");//券号无效
        }
        if (sign == null || sign.trim().equals("")) {
            Logger.info("telephone verify failed: invalid sign");
            renderText("3|签名无效");//签名无效
        }
        if (timestamp == null) {
            Logger.error("telephone verify failed: invalid timestamp");
            renderText("3|时间戳无效");//时间戳无效
        }

        //5分钟的浮动
        if (requestTimeout(timestamp, 300)) {
            Logger.error("telephone verify failed: request timeout");
            renderText("3|请求超时");//请求超时
        }
        //验证密码
        if (!validSign(timestamp, sign)) {
            Logger.info("telephone verify failed: wrong sign(暂不检查sign)");
            // renderText("6");//签名错误
//            Logger.info("暂不检查sign.");
        }

        //开始验证
        final ECoupon ecoupon = missTitleFind(coupon);

        if (ecoupon == null) {
            Logger.info("telephone verify failed: coupon not found");
            renderText("4|未找到此券");//对不起，未找到此券
        }

        SupplierUser supplierUser = SupplierUser.find("loginName=? and supplierUserType=? " +
                "and supplier.deleted=? and supplier.status=? and shop is not null",
                caller, SupplierUserType.ANDROID,
                DeletedStatus.UN_DELETED, SupplierStatus.NORMAL).first();

        if (supplierUser == null) {
            //部分商家使用分机，出口线很多，不能一一录入，这里可以使用电话号码的前面一部分进行识别。
            //偿试模糊查找验证电话机
            List<SupplierUser> supplierUserList = SupplierUser.find("supplier.id=? and supplierUserType=? " +
                    "and supplier.deleted=? and supplier.status=? and shop is not null",
                    ecoupon.goods.supplierId, SupplierUserType.ANDROID,
                    DeletedStatus.UN_DELETED, SupplierStatus.NORMAL).fetch();

            for (SupplierUser su : supplierUserList) {
                if (caller.startsWith(su.loginName)) {
                    supplierUser = su;
                    break;
                }
            }
        }

        if (supplierUser == null) {
            Logger.info("telephone verify failed: invalid caller %s", caller);
            renderText("1|您的电话还没绑定");//对不起，您的电话还没绑定，请使用已绑定的电话座机操作
        }

        //验证打电话进来的商户和券所属的商户是一样的
        if (!ecoupon.goods.supplierId.equals(supplierUser.supplier.getId())) {
            Logger.info("telephone verify failed: supplier not match");
            renderText("5|券商户错误");//对不起，您无权验证此券
        }

        if (ecoupon.isFreeze == 1) {
            Logger.info("telephone verify failed: coupon is freeze");
            renderText("2|券已经冻结");//对不起，该券无法消费
        }
        if (!ecoupon.checkVerifyTimeRegion(new Date())) {
            String info = ecoupon.getCheckInfo();
            Logger.info("telephone verify failed: %s", info);
            renderText("2|当前时间不在券可用时间段");//对不起，该券无法消费
        }

        // 设置RemoteRecallCheck所使用的标识ID，下次调用时不会再重试.
        RemoteRecallCheck.setId("COUPON_" + ecoupon.eCouponSn);
        final SupplierUser verifySupplierUser = supplierUser;
        String resultCode = TransactionRetry.run(new TransactionCallback<String>() {
            @Override
            public String doInTransaction() {
                return doVerify2(caller, verifySupplierUser, ecoupon);
            }
        });
        RemoteRecallCheck.cleanUp();

        renderText(resultCode);
    }

    private static String doVerify2(String caller, SupplierUser supplierUser, ECoupon eCoupon) {
        ECoupon ecoupon = ECoupon.findById(eCoupon.id); //因为是重试，重新得到券ID

        Logger.info("doVerify2: eCouponSN: %s", eCoupon.eCouponSn);
        if (ecoupon.status == ECouponStatus.CONSUMED) {
            String shopName = "";
            if (ecoupon.shop == null) {
                Logger.info("telephone verify failed: coupon consumed, but do not know where it consumed at");
            } else {
                shopName = ",消费门店 " + ecoupon.shop.name;
            }
            return "7|" + new SimpleDateFormat("M月d日H点m分").format(ecoupon.consumedAt) + shopName;
        } else if (ecoupon.status != ECouponStatus.UNCONSUMED) {
            Logger.info("telephone verify failed: coupon status invalid. %s", ecoupon.status);
            return "2|不可识别的券状态";//对不起，该券无法消费
        } else if (ecoupon.expireAt != null && ecoupon.expireAt.before(new Date())) {
            Logger.info("telephone verify failed: coupon expired. expiredAt: %s", new SimpleDateFormat(COUPON_DATE).format(ecoupon.expireAt));
            return "6|券已过期";//对不起，该券已过期
        } else if (ecoupon.effectiveAt != null && ecoupon.effectiveAt.after(new Date())) {
            Logger.info("telephone verify failed: coupon not been activated. effectiveAt: %s", new SimpleDateFormat(COUPON_DATE).format(ecoupon.effectiveAt));
            return "6|券未到有效期";//对不起，该券无法消费
        } else if (!ecoupon.checkVerifyTimeRegion(new Date())) {
            Logger.info("telephone verify failed: coupon not been activated. effectiveAt: %s", new SimpleDateFormat(COUPON_DATE).format(ecoupon.effectiveAt));
            return "6|券不在可用时间段";//对不起，该券无法消费
        }

        if (!ecoupon.consumeAndPayCommission(supplierUser.shop.id, supplierUser, VerifyCouponType.CLERK_MESSAGE)) {
            Logger.info("telephone verify2 failed: coupon has been refunded");
            return "2|验证失败";  //对不起，该券无法消费
        }

        String eCouponNumber = ecoupon.getMaskedEcouponSn();
        eCouponNumber = eCouponNumber.substring(eCouponNumber.lastIndexOf("*") + 1);

        String dateTime = DateUtil.getNowTime();

        // 发给消费者
        if (Play.mode.isProd()) {
            new SMSMessage("您尾号" + eCouponNumber + "的券号于" + dateTime
                    + "已成功消费，使用门店：" + supplierUser.shop.name + "。如有疑问请致电：4006865151", ecoupon.orderItems.phone,
                    ecoupon.replyCode)
                    .orderItemsId(eCoupon.orderItems.id)
                    .feeType(OrderItemsFeeType.SMS_VERIFY_NOTIFY)
                    .send();
        }

        OrderItemsFee.recordFee(eCoupon.orderItems, OrderItemsFeeType.PHONE_VERFIY,
                new BigDecimal("0.15")); //电话验证一次算0.15元

        ecoupon.verifyType = VerifyCouponType.TELEPHONE;
        ecoupon.verifyTel = caller;
        ecoupon.save();

        Logger.info("telephone verify success: eCouponSN:%s", ecoupon.eCouponSn);
        //消费成功，价值" + ecoupon.faceValue + "元
        return "0|券尾号" + eCouponNumber + "验证成功,面值" + RmbUtil.toNumberRMB(ecoupon.faceValue.toString());
    }

    /**
     * 电话验证
     *
     * @param caller    主叫号码
     * @param coupon    券号
     * @param timestamp 时间戳，UTC时间1970年1月1日零点至今的秒数，允许5分钟的上下浮动
     * @param sign      请求签名，由 分配的app_key+timestamp 拼接后进行MD5编码组成
     */
    public static void verify(final String caller, final String coupon, Long timestamp, String sign,
                              BigDecimal value) {
        Logger.info("telephone verify start: caller phone=%s, coupon=%s", caller, coupon);
        Logger.info(new Gson().toJson(request.params.allSimple()));
        if (caller == null || caller.trim().equals("")) {
            Logger.info("telephone verify failed: invalid caller");
            renderText("1");//主叫号码无效
        }
        if (coupon == null || coupon.trim().equals("")) {
            Logger.info("telephone verify failed: invalid coupon");
            renderText("2");//券号无效
        }
        if (sign == null || sign.trim().equals("")) {
            Logger.info("telephone verify failed: invalid sign");
            renderText("4");//签名无效
        }
        if (timestamp == null) {
            Logger.error("telephone verify failed: invalid timestamp");
            renderText("3");//时间戳无效
        }

        //5分钟的浮动
        if (requestTimeout(timestamp, 300)) {
            Logger.error("telephone verify failed: request timeout");
            renderText("5");//请求超时
        }
        //验证密码
        if (!validSign(timestamp, sign)) {
            Logger.info("telephone verify failed: wrong sign(暂不检查sign)");
            // renderText("6");//签名错误
//            Logger.info("暂不检查sign.");
        }

        //开始验证
        final ECoupon ecoupon = missTitleFind(coupon);

        if (ecoupon == null) {
            Logger.info("telephone verify failed: coupon not found");
            renderText("8");//对不起，未找到此券
        }

        SupplierUser supplierUser = SupplierUser.find("loginName=? and supplierUserType=? " +
                "and supplier.deleted=? and supplier.status=? and shop is not null",
                caller, SupplierUserType.ANDROID,
                DeletedStatus.UN_DELETED, SupplierStatus.NORMAL).first();

        if (supplierUser == null) {
            //部分商家使用分机，出口线很多，不能一一录入，这里可以使用电话号码的前面一部分进行识别。
            //偿试模糊查找验证电话机
            List<SupplierUser> supplierUserList = SupplierUser.find("supplier.id=? and supplierUserType=? " +
                    "and supplier.deleted=? and supplier.status=? and shop is not null",
                    ecoupon.goods.supplierId, SupplierUserType.ANDROID,
                    DeletedStatus.UN_DELETED, SupplierStatus.NORMAL).fetch();

            for (SupplierUser su : supplierUserList) {
                if (caller.startsWith(su.loginName)) {
                    supplierUser = su;
                    break;
                }
            }
        }

        if (supplierUser == null) {
            Logger.info("telephone verify failed: invalid caller %s", caller);
            renderText("7");//对不起，您的电话还没绑定，请使用已绑定的电话座机操作
        }

        //验证打电话进来的商户和券所属的商户是一样的
        if (!ecoupon.goods.supplierId.equals(supplierUser.supplier.getId())) {
            Logger.info("telephone verify failed: supplier not match");
            renderText("9");//对不起，您无权验证此券
        }

        if (ecoupon.isFreeze == 1) {
            Logger.info("telephone verify failed: coupon is freeze");
            renderText("11");//对不起，该券无法消费
        }
        if (!ecoupon.checkVerifyTimeRegion(new Date())) {
            String info = ecoupon.getCheckInfo();
            Logger.info("telephone verify failed: %s", info);
            renderText("11");//对不起，该券无法消费
        }
        // 指定门店才能消费
        // else {
        //批量验证
            /*
            if(value != null && value.compareTo(BigDecimal.ZERO) > 0){
                List<ECoupon> eCoupons = ECoupon.queryUnconsumedCouponsWithSameGoodsGroups(ecoupon);
                List<ECoupon> checkECoupons = ECoupon.selectCheckECoupons(value, eCoupons,ecoupon);

                BigDecimal consumedAmount = BigDecimal.ZERO;

                int checkedCount = 0;
                List<ECoupon> realCheckECoupon = new ArrayList<>();  //可能验证失败，所以要有一个实际真正验证成功的ecouponse
                for (ECoupon e : checkECoupons) {
                    if (e.consumeAndPayCommission(supplierUser.shop.id, null, supplierUser,
                            VerifyCouponType.SHOP, ecoupon.eCouponSn)) {
                        checkedCount += 1;
                        consumedAmount = consumedAmount.add(e.faceValue);
                        realCheckECoupon.add(e);
                    }
                }

                List<ECoupon> availableECoupons = substractECouponList(eCoupons, realCheckECoupon);
                BigDecimal availableAmount = summaryECouponsAmount(availableECoupons);
                List<String> availableECouponSNs = new ArrayList<>();
                for (ECoupon ae : availableECoupons) {
                    availableECouponSNs.add(ae.eCouponSn);
                }
                if (consumedAmount.compareTo(BigDecimal.ZERO) == 0) {
                    Logger.info("telephone verify failed: coupon not found");
                    renderText("7");//对不起，未找到此券
                    return;
                }
                if (availableECoupons.size() > 0) {
                    SMSUtil.send2("您尾号" + ecoupon.getLastCode(4)
                            + "共" + checkedCount + "张券(总面值" + consumedAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "元)于"
                            + DateUtil.getNowTime() + "已成功消费，使用门店：" + supplierUser.shop.name + "。您还有" + availableECouponSNs.size() + "张券（"
                            + StringUtils.join(availableECouponSNs, "/")
                            + "总面值" + availableAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "元）未消费。如有疑问请致电：4006865151",
                            ecoupon.orderItems.phone, ecoupon.replyCode);
                } else {
                    SMSUtil.send2("您尾号" + ecoupon.getLastCode(4)
                            + "共" + checkedCount + "张券(总面值" + consumedAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "元)于"
                            + DateUtil.getNowTime() + "已成功消费，使用门店：" + supplierUser.shop.name + "。如有疑问请致电：4006865151",
                            ecoupon.orderItems.phone, ecoupon.replyCode);
                }
                Logger.info("telephone verify: batch coupon success");
                renderText("0");
                return;//验证完成 结束了

            }else {
                //如果没有指明金额
                List<ECoupon> eCoupons = ECoupon.queryUnconsumedCouponsWithSameGoodsGroups(ecoupon);
                if(eCoupons.size() > 1){
                    //有多张券问用户是否选择批量验证
                    Logger.info("telephone verify: batch coupon");
                    renderText("100");
                    return;
                }
            }
            */
        // 设置RemoteRecallCheck所使用的标识ID，下次调用时不会再重试.
        RemoteRecallCheck.setId("COUPON_" + ecoupon.eCouponSn);
        final SupplierUser verifySupplierUser = supplierUser;
        String resultCode = TransactionRetry.run(new TransactionCallback<String>() {
            @Override
            public String doInTransaction() {
                return doVerify(caller, verifySupplierUser, ecoupon);
            }
        });
        RemoteRecallCheck.cleanUp();

        renderText(resultCode);
    }

    private static String doVerify(String caller, SupplierUser supplierUser, ECoupon eCoupon) {
        ECoupon ecoupon = ECoupon.findById(eCoupon.id); //因为是重试，重新得到券ID

        if (ecoupon.status == ECouponStatus.CONSUMED) {
            Logger.info("telephone verify failed: coupon consumed");
            return "10";//该券无法重复消费。消费时间为
        } else if (ecoupon.status != ECouponStatus.UNCONSUMED) {
            Logger.info("telephone verify failed: coupon status invalid. %s", ecoupon.status);
            return "11";//对不起，该券无法消费
        } else if (ecoupon.expireAt != null && ecoupon.expireAt.before(new Date())) {
            Logger.info("telephone verify failed: coupon expired. expiredAt: %s", new SimpleDateFormat(COUPON_DATE).format(ecoupon.expireAt));
            return "12";//对不起，该券已过期
        } else if (ecoupon.effectiveAt != null && ecoupon.effectiveAt.after(new Date())) {
            Logger.info("telephone verify failed: coupon not been activated. effectiveAt: %s", new SimpleDateFormat(COUPON_DATE).format(ecoupon.effectiveAt));
            return "11";//对不起，该券无法消费
        } else if (!ecoupon.checkVerifyTimeRegion(new Date())) {
            Logger.info("telephone verify failed: coupon not been activated. effectiveAt: %s", new SimpleDateFormat(COUPON_DATE).format(ecoupon.effectiveAt));
            return "11";//对不起，该券无法消费
        }

        if (!ecoupon.consumeAndPayCommission(supplierUser.shop.id, supplierUser, VerifyCouponType.CLERK_MESSAGE)) {
            Logger.info("telephone verify failed: coupon has been refunded");
            return "11";  //对不起，该券无法消费
        }

        String eCouponNumber = ecoupon.getMaskedEcouponSn();
        eCouponNumber = eCouponNumber.substring(eCouponNumber.lastIndexOf("*") + 1);

        String dateTime = DateUtil.getNowTime();

        // 发给消费者
        if (Play.mode.isProd()) {
            new SMSMessage("您尾号" + eCouponNumber + "的券号于" + dateTime
                    + "已成功消费，使用门店：" + supplierUser.shop.name + "。如有疑问请致电：4006865151", ecoupon.orderItems.phone,
                    ecoupon.replyCode)
                    .orderItemsId(eCoupon.orderItems.id)
                    .feeType(OrderItemsFeeType.SMS_VERIFY_NOTIFY)
                    .send();
        }

        OrderItemsFee.recordFee(eCoupon.orderItems, OrderItemsFeeType.PHONE_VERFIY,
                new BigDecimal("0.15")); //电话验证一次算0.15元

        ecoupon.verifyType = VerifyCouponType.TELEPHONE;
        ecoupon.verifyTel = caller;
        ecoupon.save();

        Logger.info("telephone verify success");
        //消费成功，价值" + ecoupon.faceValue + "元
        return "0";
    }

    /**
     * 查询已消费的面值
     *
     * @param coupon    券号
     * @param timestamp 时间戳，UTC时间1970年1月1日零点至今的秒数，允许5分钟的上下浮动
     * @param sign      请求签名，由 分配的app_key+timestamp 拼接后进行MD5编码组成
     */
    public static void consumedFaceValue(String coupon, Long timestamp, String sign) {
        Logger.info("query face value");
        Logger.info(new Gson().toJson(request.params.allSimple()));

        if (coupon == null || coupon.trim().equals("")) {
            Logger.info("query face value failed: invalid coupon");
            renderText("券号无效");//券号无效
        }

        ECoupon ecoupon = missTitleFind(coupon);

        if (ecoupon == null) {
            Logger.info("query face value failed: coupon not found");
            renderText("此券不存在");
        }
        if (timestamp == null) {
            Logger.error("query face value failed: invalid timestamp");
            renderText("时间戳无效");//时间戳无效
        }
        if (sign == null || sign.trim().equals("")) {
            Logger.error("query face value failed: invalid sign");
            renderText("签名无效");//签名无效
        }

        //5分钟的浮动
        if (requestTimeout(timestamp, 300)) {
            Logger.error("query face value failed: request timeout");
            renderText("请求超时");//请求超时
        }
        //验证密码
        if (!validSign(timestamp, sign)) {
            Logger.error("query face value failed: wrong sign");
            renderText("签名错误");
        }
        List<ECoupon> batchCoupons = ECoupon.find("byTriggerCouponSn", ecoupon.eCouponSn).fetch();
        if (batchCoupons.size() == 0) {
            renderText("此券未被消费");
            return;
        }
        BigDecimal faceValue = BigDecimal.ZERO;
        for (ECoupon c : batchCoupons) {
            faceValue = faceValue.add(c.faceValue);
        }
        //只返回整数
        renderText("" + faceValue.intValue());
    }

    /**
     * 查询消费时间
     *
     * @param coupon    券号
     * @param timestamp 时间戳，UTC时间1970年1月1日零点至今的秒数，允许5分钟的上下浮动
     * @param sign      请求签名，由 分配的app_key+timestamp 拼接后进行MD5编码组成
     */
    public static void consumedAt(String coupon, Long timestamp, String sign) {
        Logger.info("query consumed at");
        Logger.info(new Gson().toJson(request.params.allSimple()));

        if (coupon == null || coupon.trim().equals("")) {
            Logger.info("query face value failed: invalid coupon");
            renderText("券号无效");//券号无效
        }

        //开始验证
        ECoupon ecoupon = missTitleFind(coupon);

        if (ecoupon == null) {
            Logger.info("query face value failed: coupon not found");
            renderText("券号无效");
        }
        if (timestamp == null) {
            Logger.error("query face value failed: invalid timestamp");
            renderText("时间戳无效");//时间戳无效
        }
        if (sign == null || sign.trim().equals("")) {
            Logger.error("query face value failed: invalid sign");
            renderText("签名无效");//签名无效
        }

        //5分钟的浮动
        if (requestTimeout(timestamp, 300)) {
            Logger.error("query face value failed: request timeout");
            renderText("请求超时");//请求超时
        }
        //验证密码
        if (!validSign(timestamp, sign)) {
            Logger.error("query face value failed: wrong sign");
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

    /**
     * 批量消费前查询信息
     *
     * @param coupon    券号
     * @param timestamp 时间戳，UTC时间1970年1月1日零点至今的秒数，允许5分钟的上下浮动
     * @param sign      请求签名，由 分配的app_key+timestamp 拼接后进行MD5编码组成
     */
    public static void batchInfo(String coupon, Long timestamp, String sign) {
        Logger.info("query batch info");
        Logger.info(new Gson().toJson(request.params.allSimple()));

        if (coupon == null || StringUtils.isBlank(coupon)) {
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
            Logger.error("query face value failed: invalid timestamp");
            renderText("时间戳无效");//时间戳无效
        }
        if (sign == null || sign.trim().equals("")) {
            Logger.error("query face value failed: invalid sign");
            renderText("签名无效");//签名无效
        }

        //5分钟的浮动
        if (requestTimeout(timestamp, 300)) {
            Logger.error("query face value failed: request timeout");
            renderText("请求超时");//请求超时
        }
        //验证密码
        if (!validSign(timestamp, sign)) {
            Logger.error("query face value failed: wrong sign");
            renderText("签名错误");
        }

        List<ECoupon> eCoupons = ECoupon.queryUnconsumedCouponsWithSameGoodsGroups(ecoupon);
        if (eCoupons.size() == 0) {
            renderText("该券无法重复消费");
            return;
        }
        ECoupon firstCoupon = eCoupons.get(0);
        Supplier supplier = Supplier.findById(firstCoupon.goods.supplierId);
        if (supplier == null) {
            Logger.warn("telephone verify batchInfo: supplier not found");
            renderText("券不存在");
            return;
        }
        BigDecimal faceValue = BigDecimal.ZERO;
        for (ECoupon c : eCoupons) {
            faceValue = faceValue.add(c.faceValue);
        }
        renderText(supplier.otherName + "|" + eCoupons.size() + "|" + faceValue.intValue());
    }

    private static boolean requestTimeout(Long timestamp, int seconds) {
        long t = System.currentTimeMillis() / 1000;
        return Math.abs(timestamp - t) > seconds;
    }

    private static boolean validSign(Long timestamp, String sign) {
        return DigestUtils.md5Hex(APP_KEY + timestamp).equals(sign);
    }

    /**
     * 得到sourceECoupons - checkECoupons的数组.
     *
     * @param sourceECoupons
     * @param checkECoupons
     * @return
     */
    private static List<ECoupon> substractECouponList(List<ECoupon> sourceECoupons,
                                                      List<ECoupon> checkECoupons) {
        Set<Long> checkECouponIdSet = new HashSet<>();
        for (ECoupon e : checkECoupons) {
            checkECouponIdSet.add(e.id);
        }
        List<ECoupon> results = new ArrayList<>();
        for (ECoupon e : sourceECoupons) {
            if (!checkECouponIdSet.contains(e.id)) {
                results.add(e);
            }
        }
        return results;
    }

    /**
     * 模糊查找券号.
     * <p/>
     * 前置第1位券可能在电话验证时没有传入，所以需要加入模糊查找。
     * 前置模糊查找不能使用like方式，会导致全表扫描。
     *
     * @param couponSn
     * @return
     */
    private static ECoupon missTitleFind(String couponSn) {
        ECoupon coupon = ECoupon.find("eCouponSn = ?", couponSn).first();
        if (coupon != null) {
            return coupon;
        }
        // 加一个前置数字再找一次
        String[] eCouponSNs = new String[10];
        for (int i = 0; i < 10; i++) {
            eCouponSNs[i] = i + couponSn;
        }
        return ECoupon.find("eCouponSn in (?,?,?,?,?,?,?,?,?,?)", eCouponSNs).first();
    }

    private static BigDecimal summaryECouponsAmount(List<ECoupon> ecoupons) {
        BigDecimal amount = BigDecimal.ZERO;
        for (ECoupon ecoupon : ecoupons) {
            amount = amount.add(ecoupon.faceValue);
        }
        return amount;
    }
}
