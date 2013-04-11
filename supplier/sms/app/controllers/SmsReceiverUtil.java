package controllers;

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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiverUtil {

    /**
     * 店员验证的情况
     *
     * @param mobile 手机
     * @param msg    券号
     * @param code   回复代码
     */
    private static String checkClerk(String mobile, String msg, String code) {
        String[] couponArray = msg.split("#");

        //验证店员是否存在
        String couponNumber = "";
        Logger.info("mobile=" + mobile + ", msg=" + msg + ", code=" + code);
        int couponCount = couponArray.length;
        if (couponCount > 0) {
            for (int i = 1; i < couponCount; i++) {
                couponNumber = couponArray[i];
                if (!FieldCheckUtil.isNumeric(couponNumber)) {
                    sendSmsToClerk("您输入的券号" + couponNumber + "无效，请确认！", mobile, code);
                    return ("券号无效！");
                }

                ECoupon ecoupon = ECoupon.query(couponNumber, null);

                if (ecoupon == null) {
                    sendSmsToClerk("您输入的券号" + couponNumber + "不存在，请与顾客确认，如有疑问请致电：4006865151", mobile, code);
                    return ("您输入的券号" + couponNumber + "不存在，请确认！");
                } else {
                    if (ecoupon.isFreeze == 1) {
                        sendSmsToClerk("该券已被冻结,如有疑问请致电：4006865151", mobile, code);
                        return ("该券已被冻结");
                    }
                    if (!ecoupon.checkVerifyTimeRegion(new Date())) {
                        String info = ecoupon.getCheckInfo();
                        sendSmsToClerk(info + "如有疑问请致电：4006865151", mobile, code);
                        return (info);
                    }
                    Long supplierId = ecoupon.goods.supplierId;

                    Supplier supplier = Supplier.findById(supplierId);

                    if (supplier == null || supplier.deleted == DeletedStatus.DELETED) {
                        String supplierName = (supplier == null) ? "" : supplier.fullName;
                        sendSmsToClerk(supplierName + "未在一百券登记使用，如有疑问请致电：4006865151", mobile, code);
                        return (supplierName + "未在一百券登记使用");
                    }

                    if (supplier.status == SupplierStatus.FREEZE) {
                        sendSmsToClerk(supplier.fullName + "已被一百券锁定，如有疑问请致电：4006865151", mobile, code);
                        return (supplier.fullName + "已被一百券锁定");
                    }

                    SupplierUser supplierUser = SupplierUser.findByMobileAndSupplier(mobile, supplier);

                    if (supplierUser == null) {
                        sendSmsToClerk("请确认券号" + couponNumber + "(" + supplier.fullName + ")是否本店商品，或店号手机号是否已在一百券登记。如有疑问请致电：4006865151", mobile, code);
                        return ("请确认券号" + couponNumber + "(" + supplier.fullName + ")是否本店商品，或店号手机号是否已在一百券登记");
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

                    String consumerPhone = ecoupon.orderItems.phone;
                    if (ecoupon.expireAt.before(new Date())) {
                        sendSmsToClerk("券号" + couponNumber + "已过期，无法进行消费。如有疑问请致电：4006865151", mobile, code);
                        return ("券号" + couponNumber + "已过期，无法进行消费");
                    } else if (ecoupon.status == ECouponStatus.UNCONSUMED) {
                        String coupon = ecoupon.getMaskedEcouponSn();
                        if (!ecoupon.checkVerifyTimeRegion(new Date())) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat(ECoupon.TIME_FORMAT);
                            // 发给消费者
                            sendSmsToClerk(getMaskedMobile(consumerPhone) + "的" + coupon + "券只能在" + dateFormat.format(ecoupon.goods.useBeginTime)
                                    + "至" + dateFormat.format(ecoupon.goods.useEndTime) + "时间段内消费，现在不能消费。客服4006865151",
                                    supplierUser.mobile, code);
                            return ("您尾号" + getMaskedMobile(consumerPhone) + "的" + coupon + "券只能在" + dateFormat.format(ecoupon.goods.useBeginTime)
                                    + "至" + dateFormat.format(ecoupon.goods.useEndTime) + "时间段内消费，现在不能消费");
                        }

                        if (!ecoupon.consumeAndPayCommission(shopId, supplierUser, VerifyCouponType.CLERK_MESSAGE)){
                            return ("您尾号" + getMaskedMobile(consumerPhone) + "的" + coupon + "券已经退款，现在不能消费");
                        }

                        coupon = coupon.substring(coupon.lastIndexOf("*") + 1);
                        String dateTime = DateUtil.getNowTime();


                        // 发给店员
                        sendSmsToClerk(getMaskedMobile(consumerPhone) + "尾号" + coupon + "券（面值" + ecoupon
                                .faceValue + "元）于" + dateTime + "在" + shopName + "验证成功。客服4006865151", supplierUser.mobile, code);
                        // 发给消费者
                        sendSmsToConsumer("您尾号" + coupon + "券于" + dateTime
                                + "成功消费，门店：" + shopName + "。客服4006865151", consumerPhone, code);
                        return ("您尾号" + coupon + "的券号于" + dateTime
                                + "已成功消费，门店：" + shopName + "。客服4006865151");
                    } else if (ecoupon.status == ECouponStatus.CONSUMED) {
                        String couponLastCode = ecoupon.getLastCode(4);
                        SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm");
                        // 发给店员
                        resendSmsToClerk(getMaskedMobile(consumerPhone) + "尾号" + couponLastCode + "券（" + ecoupon
                                .faceValue + "元）不能重复消费，已于" + df.format(ecoupon.consumedAt) + "在" + shopName + "消费过", supplierUser.mobile, code);

                        return ("券号" + couponNumber + "已消费，无法再次消费");
                    }
                }
            }
        }
        sendSmsToClerk("券号格式错误，单个发送\"#券号\"，多个发送\"#券号#券号\"，如有疑问请致电：4006865151",
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
    private static String checkConsumer(String mobile, String msg, String code) {
        // 消费者验证逻辑
        List<ECoupon> ecoupons = ECoupon.findByMobileAndCode(mobile, code);
        if (ecoupons == null || ecoupons.size() == 0) {
            if ("0000".equals(code)) {
                sendSmsToConsumer("券号格式错误，单个发送\"#券号\"，多个发送\"#券号#券号\"，如有疑问请致电：4006865151",
                        mobile, code);
            } else {
                sendSmsToConsumer("券号无法验证，请确认是否使用您购买时的手机号发送验证短信。如有疑问请致电：4006865151", mobile, code);
            }
            return ("Not Found the coupon");
        }


        ECoupon ecoupon = ecoupons.get(0);

        if (ecoupon.isFreeze == 1) {
            sendSmsToClerk("该券已被冻结,如有疑问请致电：4006865151", mobile, code);
            return ("该券已被冻结");
        }
        if (!ecoupon.checkVerifyTimeRegion(new Date())) {
            String info = ecoupon.getCheckInfo();
            sendSmsToClerk(info + "如有疑问请致电：4006865151", mobile, code);
            return (info);
        }

        Supplier supplier = Supplier.findById(ecoupon.goods.supplierId);

        if (supplier == null || supplier.deleted == DeletedStatus.DELETED) {
            String supplierName = (supplier == null) ? "" : supplier.fullName;
            sendSmsToConsumer( supplierName + "未在一百券登记使用，请致电4006865151咨询", mobile, code);
            return ( supplierName + "未在一百券登记使用");
        }

        if (supplier.status == SupplierStatus.FREEZE) {
            sendSmsToConsumer( supplier.fullName + "已被一百券锁定，请致电4006865151咨询", mobile, code);
            return ( supplier.fullName + "已被一百券锁定");
        }

        Logger.info("supperId=%s, jobNumber=%s", ecoupon.goods.supplierId, msg);
        SupplierUser supplierUser = SupplierUser.find("from SupplierUser where deleted = ? and supplier.id=? and jobNumber=?", DeletedStatus.UN_DELETED, ecoupon.goods.supplierId, msg).first();

        if (supplierUser == null) {
            // 发给消费者
            sendSmsToConsumer("店员工号无效，请核实工号是否正确或是否是" + supplier.fullName + "门店。如有疑问请致电：4006865151",
                    mobile, code);
            return ("店员工号无效，请核实工号是否正确或是否是" + supplier.fullName + "门店");
        }

        // 有多张券，则需要指定验证金额.
        if (ecoupons.size() > 1) {
            BigDecimal amount = BigDecimal.ZERO;  //总面值
            for (ECoupon ec : ecoupons) {
                amount = amount.add(ec.faceValue);
            }
            amount = amount.setScale(2, BigDecimal.ROUND_HALF_UP); //四舍五入

            // 发给店员
            resendSmsToClerk( getMaskedMobile(mobile) +
                    "有多张可用券，请指导顾客回复数字工号*使用金额，如\"100112*200\"",
                    supplierUser.mobile, code);
            // 发给消费者
            resendSmsToConsumer("您有多张可用券(总面值" + amount +
                    "元)，请回复店员数字工号*使用金额，如\"100112*200\"，系统自动选择合适的券验证",
                    mobile, code);

            return ("有多张可用券，请回复数字工号*使用金额");
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
                sendSmsToConsumer("您的券号已过期，无法进行消费。如有疑问请致电：4006865151", mobile, code);
                return ("您的券号已过期，无法进行消费。如有疑问请致电：4006865151");
            } else if (ecoupon.status == ECouponStatus.UNCONSUMED) {
                String couponLastCode = ecoupon.getLastCode(4);
                String dateTime = DateUtil.getNowTime();
                if (!ecoupon.checkVerifyTimeRegion(new Date())) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(ECoupon.TIME_FORMAT);
                    // 发给消费者
                    sendSmsToConsumer("您尾号" + couponLastCode + "券只能在" + dateFormat.format(ecoupon.goods.useBeginTime)
                            + "至" + dateFormat.format(ecoupon.goods.useEndTime) + "时间段内消费，现在不能消费。客服4006865151",
                            supplierUser.mobile, code);
                    return ("您尾号" + couponLastCode + "券只能在" + dateFormat.format(ecoupon.goods.useBeginTime)
                            + "至" + dateFormat.format(ecoupon.goods.useEndTime) + "时间段内消费，现在不能消费");
                }

                if(!ecoupon.consumeAndPayCommission(shopId, supplierUser, VerifyCouponType.CONSUMER_MESSAGE)){
                    return ("您尾号" + couponLastCode + "的券已经退款，现在不能消费");
                }

                // 发给店员
                sendSmsToClerk(getMaskedMobile(mobile) + "尾号" + couponLastCode + "券（面值" + ecoupon
                        .faceValue + "元）于" + dateTime + "在" + shopName + "验证成功。客服4006865151", supplierUser.mobile, code);
                // 发给消费者
                sendSmsToConsumer("您尾号" + couponLastCode + "券于" + dateTime
                        + "成功消费，门店：" + shopName + "。客服4006865151", mobile, code);
                return ("您尾号" + couponLastCode + "的券号于" + dateTime
                        + "已成功消费，门店：" + shopName + "。客服4006865151");
            } else if (ecoupon.status == ECouponStatus.CONSUMED) {
                String couponLastCode = ecoupon.getLastCode(4);
                SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm");
                // 发给店员
                resendSmsToClerk(getMaskedMobile(mobile) + "尾号" + couponLastCode + "券（" + ecoupon
                        .faceValue + "元）不能重复消费，已于" + df.format(ecoupon.consumedAt) + "在" + shopName + "消费过", supplierUser.mobile, code);
                // 发给消费者
                resendSmsToConsumer("您尾号" + couponLastCode + "券不能重复消费，已于" + df.format(ecoupon.consumedAt)
                        + "在" + shopName + "消费过", mobile, code);

                return ("您的券号已消费，无法再次消费。如有疑问请致电：4006865151");
            }
        }

        sendSmsToConsumer("您的券号不能在" + shopName + "消费，请与店员确认。如有疑问请致电：4006865151", mobile, code);
        return ("您的券号不能在" + shopName + "消费，mobile:" + mobile);
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
        Logger.info("SmsReceiverUtil: mobile=" + mobile + ", msg=" + msg + ", code=" + code);

        // 非法手机号
        if (mobile == null || mobile.length() < 10) {
            Logger.warn("手机号%s非法", mobile);
            return "无效的手机号" + mobile;
        }

        String result = null;
        if (isClerkMessage(mobile, msg)) {
            // 店员验证
            result = SmsReceiverUtil.checkClerk(mobile, msg, code);
        } else if (isCoumerMessageWithAmount(msg)) {
            String[] msgs = msg.split("\\*");  //前面正则表达式保证了msgs两个值都是数字型
            // 消费者验证的情况
            result = SmsReceiverUtil.checkConsumerWithAmount(mobile, msgs[0], new BigDecimal(msgs[1]), code);
        } else if (isConsumerMessageWithoutAmount(msg)) {
            // 消费者验证的情况
            result = SmsReceiverUtil.checkConsumer(mobile, msg, code);
        } else {
            // 如果手机号是店员
            if (SupplierUser.checkMobile(mobile)) {
                SMSUtil.send("券号格式错误，单个发送\"#券号\"，多个发送\"#券号#券号\"，如有疑问请致电：4006865151",
                        mobile, code);
                result = "Unsupport Message";
            } else {
                SMSUtil.send("不支持的命令，券验证请回复店员数字工号；或店员数字工号*验证金额，如299412*200",
                        mobile, code);
                result = "Unsupport Message";
            }
        }
        return result;
    }

    private static String checkConsumerWithAmount(String mobile, String jobNumber,
                                                  BigDecimal amount, String code) {
        return null;
    }

    private static Pattern CONSUMER_MSG_WITH_AMOUNT = Pattern.compile("(\\d+)\\*(\\d+)");

    /**
     * 检查消息是否为消费者消息
     *
     * @param msg
     * @return
     */
    private static boolean isConsumerMessageWithoutAmount(String msg) {
        return FieldCheckUtil.isNumeric(msg);
    }

    private static boolean isCoumerMessageWithAmount(String msg) {
        Matcher m = CONSUMER_MSG_WITH_AMOUNT.matcher(msg);

        return m.matches();
    }

    /**
     * 检查消息是否为店员消息.
     *
     * @param msg
     * @return
     */
    private static boolean isClerkMessage(String mobile, String msg) {
        return SupplierUser.checkMobile(mobile) && msg.contains("#");
    }
}
