package models.weixin;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.FieldCheckUtil;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.Shop;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
import play.Logger;
import util.extension.ExtensionResult;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: tanglq
 * Date: 13-3-22
 * Time: 上午12:27
 */
public class WeixinVerifyECouponInvocation extends WeixinInvocation {
    @Override
    public ExtensionResult execute(WeixinContext context) {
        String msg = context.weixinRequest.selectTextTrim("Content");
        SupplierUser supplierUser = SupplierUser.find("weixinOpenId=?", context.weixinRequest.fromUserName).first();
        if (supplierUser == null) {
            context.resultText = "您还没有绑定微信，请输入6位身份识别码。";
            return ExtensionResult.SUCCESS;
        }
        context.resultText = checkClerk(supplierUser, msg);
        return ExtensionResult.SUCCESS;
    }

    /**
     * 我们约定10位字符为券验证请求.
     * @param context
     * @return
     */
    @Override
    public boolean match(WeixinContext context) {
        if (context.weixinRequest.msgType == WeixinMessageType.TEXT) {
            String content = context.weixinRequest.selectTextTrim("Content");
            return content.length() == 10;  // FIXME: 检查为10位数字
        }
        return false;
    }


    /**
     * 店员验证的情况
     *
     */
    private static String checkClerk(SupplierUser supplierUser, String msg) {
        String[] couponArray = msg.split(",");

        //验证店员是否存在
        Logger.info("supplierUser=" + supplierUser.loginName + ", msg=" + msg);
        int couponCount = couponArray.length;
        if (couponCount > 0) {
            Logger.info("couponCount=%d", couponCount);
            for (String couponNumber : couponArray) {
                Logger.info("couponNumber=%s", couponNumber);
                if (!FieldCheckUtil.isNumeric(couponNumber)) {
                    return ("券号无效！");
                }

                ECoupon ecoupon = ECoupon.query(couponNumber, null);

                if (ecoupon == null) {
                    return ("您输入的券号" + couponNumber + "不存在，请确认！");
                } else {
                    if (ecoupon.isFreeze == 1) {
                        return ("该券已被冻结");
                    }
                    if (!ecoupon.checkVerifyTimeRegion(new Date())) {
                        String info = ecoupon.getCheckInfo();
                        return (info);
                    }
                    Long supplierId = ecoupon.goods.supplierId;

                    Supplier supplier = Supplier.findById(supplierId);

                    if (supplier == null || supplier.deleted == DeletedStatus.DELETED) {
                        String supplierName = (supplier == null) ? "" : supplier.fullName;
                        return (supplierName + "未在一百券登记使用");
                    }

                    if (!supplierUser.supplier.id.equals(supplier.id)) {
                        return ("请确认券号" + couponNumber + "(" + supplier.fullName + ")是否本店商品");
                    }

                    if (supplier.status == SupplierStatus.FREEZE) {
                        return (supplier.fullName + "已被一百券锁定");
                    }

                    String shopName = "未知";
                    Long shopId = null;
                    if (supplierUser.shop != null) {
                        //判断该券是否属于所在消费门店
                        shopId = supplierUser.shop.id;
                        //取得消费门店
                        Shop shop = Shop.findById(shopId);
                        shopName = shop.name;
                    } else {
                        return "您没有绑定门店，不能使用微信进行券验证！";
                    }

                    String consumerPhone = ecoupon.orderItems.phone;
                    if (ecoupon.expireAt.before(new Date())) {
                        return ("券号" + couponNumber + "已过期，无法进行消费");
                    } else if (ecoupon.status == ECouponStatus.UNCONSUMED) {
                        String coupon = ecoupon.getMaskedEcouponSn();
                        if (!ecoupon.checkVerifyTimeRegion(new Date())) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat(ECoupon.TIME_FORMAT);
                            return ("尾号" + getMaskedMobile(consumerPhone) + "的" + coupon + "券只能在" + dateFormat.format(ecoupon.goods.useBeginTime)
                                    + "至" + dateFormat.format(ecoupon.goods.useEndTime) + "时间段内消费，现在不能消费");
                        }

                        if (!ecoupon.consumeAndPayCommission(shopId, supplierUser, VerifyCouponType.WEIXIN)){
                            return ("尾号" + getMaskedMobile(consumerPhone) + "的" + coupon + "券已经退款，现在不能消费");
                        }

                        coupon = coupon.substring(coupon.lastIndexOf("*") + 1);
                        String dateTime = DateUtil.getNowTime();

                        // 发给消费者
                        sendSmsToConsumer("您尾号" + coupon + "券于" + dateTime
                                + "成功消费，门店：" + shopName + "。客服4006865151", consumerPhone, ecoupon.replyCode);
                        return ("尾号" + coupon + "的券号于" + dateTime
                                + "成功消费，门店：" + shopName + "。客服4006865151");
                    } else if (ecoupon.status == ECouponStatus.CONSUMED) {
                        return ("券号" + couponNumber + "已消费，无法再次消费");
                    }
                }
            }
        }
        return ("券号无效！");
    }

    /**
     * 处理消费者手机号
     *
     * @param mobile 手机号
     * @return 处理后的手机号
     */
    public static String getMaskedMobile(String mobile) {
        StringBuilder sn = new StringBuilder();
        sn.append(mobile.substring(0, 3) + "*****" + mobile.substring(8, 11));
        return sn.toString();
    }


    private static void sendSmsToConsumer(String message, String mobile, String code) {
        SMSUtil.send(message, mobile, code);
    }

    private static void resendSmsToConsumer(String message, String mobile, String code) {
        SMSUtil.send2(message, mobile, code);
    }

}
