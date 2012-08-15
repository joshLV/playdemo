package controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.Shop;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
import play.Logger;
import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.FieldCheckUtil;

public class SmsReceiverUtil {

    /**
     * 店员验证的情况
     *
     * @param mobile 手机
     * @param msg    券号
     * @param code   回复代码
     */
    public static String checkClerk(String mobile, String msg, String code) {
        String[] couponArray = msg.split("#");
        
        //验证店员是否存在
        String couponNumber = "";
        Logger.info("mobile=" + mobile + ", msg=" + msg + ", code=" + code);
        int couponCount = couponArray.length;
        if (couponCount > 0) {
            for (int i = 1; i < couponCount; i++) {
                couponNumber = couponArray[i];
                if (!FieldCheckUtil.isNumeric(couponNumber)) {
                    sendSmsToClerk("【券市场】您输入的券号" + couponNumber + "无效，请确认！", mobile, code);
                    return ("券号无效！");
                }

                ECoupon ecoupon = ECoupon.query(couponNumber, null);

                if (ecoupon == null) {
                    sendSmsToClerk("【券市场】您输入的券号" + couponNumber + "不存在，请与消费者确认，如有疑问请致电：400-6262-166", mobile, code);
                    return ("【券市场】您输入的券号" + couponNumber + "不存在，请确认！");
                } else {
                    Long supplierId = ecoupon.goods.supplierId;

                    Supplier supplier = Supplier.findById(supplierId);

                    if (supplier == null || supplier.deleted == DeletedStatus.DELETED) {
                        sendSmsToClerk("【券市场】" + supplier.fullName + "未在券市场登记使用，如有疑问请致电：400-6262-166", mobile, code);
                        return ("【券市场】" + supplier.fullName + "未在券市场登记使用");
                    }

                    if (supplier.status == SupplierStatus.FREEZE) {
                        sendSmsToClerk("【券市场】" + supplier.fullName + "已被券市场锁定，如有疑问请致电：400-6262-166", mobile, code);
                        return ("【券市场】" + supplier.fullName + "已被券市场锁定");
                    }

                    SupplierUser supplierUser = SupplierUser.findByMobileAndSupplier(mobile, supplier);

                    if (supplierUser == null) {
                        sendSmsToClerk("【券市场】店员工号无效，请核实工号是否正确或是否是" + supplier.fullName + "门店。如有疑问请致电：400-6262-166", mobile, code);
                        return ("【券市场】店员工号无效，请核实工号是否正确或是否是" + supplier.fullName + "门店");
                    }


                    String shopName = "未知";
                    Long shopId = null;
                    if (supplierUser.shop != null) {
                        //判断该券是否属于所在消费门店
                        shopId = supplierUser.shop.id;
                        //取得消费门店
                        Shop shop = Shop.findById(shopId);
                        shopName = shop.name;
                    }

                    if (ecoupon.expireAt.before(new Date())) {
                        sendSmsToClerk("【券市场】券号" + couponNumber + "已过期，无法进行消费。如有疑问请致电：400-6262-166", mobile, code);
                        return ("【券市场】券号" + couponNumber + "已过期，无法进行消费");
                    } else if (ecoupon.status == ECouponStatus.UNCONSUMED) {
                        ecoupon.consumeAndPayCommission(shopId, null, supplierUser, VerifyCouponType.CLERK_MESSAGE);
                        String coupon = ecoupon.getMaskedEcouponSn();
                        coupon = coupon.substring(coupon.lastIndexOf("*") + 1);

                        String dateTime = DateUtil.getNowTime();

                        String consumerPhone = ecoupon.orderItems.phone;

                        // 发给店员
                        sendSmsToClerk("【券市场】" + getMaskedMobile(consumerPhone) + "尾号" + coupon + "券（面值" + ecoupon
                                .faceValue + "元）于" + dateTime + "在" + shopName + "验证成功。客服4006262166", supplierUser.mobile, code);
                        // 发给消费者
                        sendSmsToConsumer("【券市场】您尾号" + coupon + "券于" + dateTime
                                + "成功消费，门店：" + shopName + "。客服4006262166", mobile, code);
                        return ("【券市场】您尾号" + coupon + "的券号于" + dateTime
                                + "已成功消费，门店：" + shopName + "。客服4006262166");
                    } else if (ecoupon.status == ECouponStatus.CONSUMED) {
                        String couponLastCode = ecoupon.getLastCode(4);
                        SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm");
                        // 发给店员
                        resendSmsToClerk("【券市场】" + getMaskedMobile(mobile) + "尾号" + couponLastCode + "券（" + ecoupon
                                .faceValue + "元）不能重复消费，已于" + df.format(ecoupon.consumedAt) + "在" + shopName + "消费过", supplierUser.mobile, code);
                        
                        return ("【券市场】券号" + couponNumber + "已消费，无法再次消费");
                    }
                }
            }
        }
        sendSmsToClerk("【券市场】券号格式错误，单个发送\"#券号\"，多个发送\"#券号#券号\"，如有疑问请致电：400-6262-166",
                mobile, code);
        return ("券号无效！");

    }

    /**
     * 消费者验证的情况
     *
     * @param mobile 手机
     * @param msg    工号
     * @param code   回复代码
     */
    public static String checkConsumer(String mobile, String msg, String code) {
        // 消费者验证逻辑
        ECoupon ecoupon = ECoupon.findByMobileAndCode(mobile, code);
        if (ecoupon == null) {
            if ("0000".equals(code)) {
                sendSmsToConsumer("【券市场】券号格式错误，单个发送\"#券号\"，多个发送\"#券号#券号\"，如有疑问请致电：400-6262-166",
                        mobile, code);
            } else {
                sendSmsToConsumer("【券市场】券号无法验证，请确认是否使用您购买时的手机号发送验证短信。如有疑问请致电：4006262166", mobile, code);
            }
            return ("Not Found the coupon");

        }

        Supplier supplier = Supplier.findById(ecoupon.goods.supplierId);

        if (supplier == null || supplier.deleted == DeletedStatus.DELETED) {
            sendSmsToConsumer("【券市场】" + supplier.fullName + "未在券市场登记使用，请致电400-6262-166咨询", mobile, code);
            return ("【券市场】" + supplier.fullName + "未在券市场登记使用");
        }

        if (supplier.status == SupplierStatus.FREEZE) {
            sendSmsToConsumer("【券市场】" + supplier.fullName + "已被券市场锁定，请致电400-6262-166咨询", mobile, code);
            return ("【券市场】" + supplier.fullName + "已被券市场锁定");
        }

        Logger.info("supperId=%s, jobNumber=%s", ecoupon.goods.supplierId, msg);
        SupplierUser supplierUser = SupplierUser.find("from SupplierUser where deleted = ? and supplier.id=? and jobNumber=?", DeletedStatus.UN_DELETED, ecoupon.goods.supplierId, msg).first();

        if (supplierUser == null) {
            // 发给消费者
            sendSmsToConsumer("【券市场】店员工号无效，请核实工号是否正确或是否是" + supplier.fullName + "门店。如有疑问请致电：400-6262-166",
                    mobile, code);
            return ("【券市场】店员工号无效，请核实工号是否正确或是否是" + supplier.fullName + "门店");
        }

        boolean canNotUserInThisShop = false;

        String shopName = "未知";
        Long shopId = null;
        if (supplierUser.shop != null) {
            //判断该券是否属于所在消费门店
            shopId = supplierUser.shop.id;
            if (!ecoupon.isBelongShop(shopId)) {
                canNotUserInThisShop = true;
            }
            //取得消费门店
            Shop shop = Shop.findById(shopId);
            shopName = shop.name;
        }

        //门店不在
        if (!canNotUserInThisShop) {
            if (ecoupon.expireAt.before(new Date())) {
                //过期
                sendSmsToConsumer("【券市场】您的券号已过期，无法进行消费。如有疑问请致电：400-6262-166", mobile, code);
                return ("【券市场】您的券号已过期，无法进行消费。如有疑问请致电：400-6262-166");
            } else if (ecoupon.status == ECouponStatus.UNCONSUMED) {
                ecoupon.consumeAndPayCommission(shopId, null, supplierUser, VerifyCouponType.CONSUMER_MESSAGE);
                String couponLastCode = ecoupon.getLastCode(4);
                String dateTime = DateUtil.getNowTime();

                // 发给店员
                sendSmsToClerk("【券市场】" + getMaskedMobile(mobile) + "尾号" + couponLastCode + "券（面值" + ecoupon
                        .faceValue + "元）于" + dateTime + "在" + shopName + "验证成功。客服4006262166", supplierUser.mobile, code);
                // 发给消费者
                sendSmsToConsumer("【券市场】您尾号" + couponLastCode + "券于" + dateTime
                        + "成功消费，门店：" + shopName + "。客服4006262166", mobile, code);
                return ("【券市场】您尾号" + couponLastCode + "的券号于" + dateTime
                        + "已成功消费，门店：" + shopName + "。客服4006262166");
            } else if (ecoupon.status == ECouponStatus.CONSUMED) {
                String couponLastCode = ecoupon.getLastCode(4);
                SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm");
                // 发给店员
                resendSmsToClerk("【券市场】" + getMaskedMobile(mobile) + "尾号" + couponLastCode + "券（" + ecoupon
                        .faceValue + "元）不能重复消费，已于" + df.format(ecoupon.consumedAt) + "在" + shopName + "消费过", supplierUser.mobile, code);
                // 发给消费者
                resendSmsToConsumer("【券市场】您尾号" + couponLastCode + "券不能重复消费，已于" + df.format(ecoupon.consumedAt)
                        + "在" + shopName + "消费过", mobile, code);
                
                return ("【券市场】您的券号已消费，无法再次消费。如有疑问请致电：400-6262-166");
            }
        }

        sendSmsToConsumer("【券市场】您的券号不能在" + shopName + "消费，请与店员确认。如有疑问请致电：400-6262-166", mobile, code);
        return ("【券市场】您的券号不能在" + shopName + "消费，mobile:" + mobile);
    }

    private static void sendSmsToConsumer(String message, String mobile, String code) {
        SMSUtil.send(message, mobile, code);
    }

    private static void sendSmsToClerk(String message, String mobile, String code) {
        SMSUtil.send2(message, mobile, code);
    }    

    private static void resendSmsToConsumer(String message, String mobile, String code) {
        SMSUtil.send2(message, mobile, code);
    }

    private static void resendSmsToClerk(String message, String mobile, String code) {
        SMSUtil.send(message, mobile, code);
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


    public static String processMessage(String mobile, String msg,
            String code) {
        Logger.info("LingShiSMS: mobile=" + mobile + ", msg=" + msg + ", code=" + code);

        // 非法手机号
        if (mobile == null || mobile.length() < 10) {
            Logger.warn("手机号%s非法", mobile);
            return "无效的手机号" + mobile;
        }
        
        String result = null;
        if (msg.contains("#")) {
            // 店员验证
            result = SmsReceiverUtil.checkClerk(mobile, msg, code);
        } else if (FieldCheckUtil.isNumeric(msg)) {
            // 消费者验证的情况
            result = SmsReceiverUtil.checkConsumer(mobile, msg, code);
        } else {
            SMSUtil.send("【券市场】券号格式错误，单个发送\"#券号\"，多个发送\"#券号#券号\"，如有疑问请致电：400-6262-166",
                    mobile, code);
            result = "Unsupport Message";
        }
        return result;
    }
}
