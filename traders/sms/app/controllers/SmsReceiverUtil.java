package controllers;

import play.Logger;
import java.util.Date;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.Shop;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
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
                    SMSUtil.send("【券市场】您输入的券号" + couponNumber + "无效，请确认！", mobile, code);
                    return ("券号无效！");
                }

                ECoupon ecoupon = ECoupon.query(couponNumber, null);

                if (ecoupon == null) {
                    SMSUtil.send("【券市场】您输入的券号" + couponNumber + "不存在，请与消费者确认，如有疑问请致电：400-6262-166", mobile, code);
                    return ("【券市场】您输入的券号" + couponNumber + "不存在，请确认！");
                } else {
                    Long supplierId = ecoupon.goods.supplierId;

                    Supplier supplier = Supplier.findById(supplierId);

                    if (supplier == null || supplier.deleted == DeletedStatus.DELETED) {
                        SMSUtil.send("【券市场】" + supplier.fullName + "未在券市场登记使用，如有疑问请致电：400-6262-166", mobile, code);
                        return ("【券市场】" + supplier.fullName + "未在券市场登记使用");
                    }

                    if (supplier.status == SupplierStatus.FREEZE) {
                        SMSUtil.send("【券市场】" + supplier.fullName + "已被券市场锁定，如有疑问请致电：400-6262-166", mobile, code);
                        return ("【券市场】" + supplier.fullName + "已被券市场锁定");
                    }

                    SupplierUser supplierUser = SupplierUser.findByMobileAndSupplier(mobile, supplier);

                    if (supplierUser == null) {
                        SMSUtil.send("【券市场】店员工号无效，请核实工号是否正确或是否是" + supplier.fullName + "门店。如有疑问请致电：400-6262-166", mobile, code);
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
                        SMSUtil.send("【券市场】您的券号已过期，无法进行消费。如有疑问请致电：400-6262-166", mobile, code);
                        return ("【券市场】您的券号已过期，无法进行消费。如有疑问请致电：400-6262-166");
                    } else if (ecoupon.status == ECouponStatus.UNCONSUMED) {
                        ecoupon.consumeAndPayCommission(shopId, supplierUser, VerifyCouponType.CLERK_MESSAGE);
                        String coupon = ecoupon.getMaskedEcouponSn();
                        coupon = coupon.substring(coupon.lastIndexOf("*") + 1);

                        String dateTime = DateUtil.getNowTime();
                        
                        String consumerPhone = ecoupon.orderItems.phone;

                        // 发给店员
                        SMSUtil.send("【券市场】" + getMaskedMobile(consumerPhone) + "的尾号" + coupon + "券（面值" + ecoupon
                                .faceValue + "元）于" + dateTime + "验证成功，门店：" + shopName + "。客服4006262166", supplierUser.mobile, code);
                        // 发给消费者
                        SMSUtil.send("【券市场】您尾号" + coupon + "券于" + dateTime
                                + "成功消费，门店：" + shopName + "。客服4006262166", mobile, code);
                        return ("【券市场】您尾号" + coupon + "的券号于" + dateTime
                                + "已成功消费，门店：" + shopName + "。客服4006262166");
                    } else if (ecoupon.status == ECouponStatus.CONSUMED) {
                        SMSUtil.send("【券市场】您的券号已消费，无法再次消费。如有疑问请致电：400-6262-166", mobile, code);
                        return ("【券市场】您的券号已消费，无法再次消费。如有疑问请致电：400-6262-166");
                    }
                }
            }
        }
        SMSUtil.send("【券市场】券号格式错误，单个发送\"#券号\"，多个发送\"#券号#券号\"，如有疑问请致电：400-6262-166",
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
                SMSUtil.send("【券市场】券号格式错误，单个发送\"#券号\"，多个发送\"#券号#券号\"，如有疑问请致电：400-6262-166",
                        mobile, code);
            } else {
                SMSUtil.send("【券市场】店员工号无效，请核实工号是否正确!如有疑问请致电：400-6262-166", mobile, code);
            }
            return ("Not Found the coupon");

        }

        Supplier supplier = Supplier.findById(ecoupon.goods.supplierId);
        SupplierUser supplierUser = SupplierUser.find("from SupplierUser where deleted = ? and supplier.id=? and jobNumber=?", DeletedStatus.UN_DELETED,  ecoupon.goods.supplierId, msg).first();

        if (supplierUser == null) {
            // 发给消费者
            SMSUtil.send("【券市场】店员工号无效，请核实工号是否正确或是否是" + supplier.fullName + "门店。如有疑问请致电：400-6262-166",
                    mobile, code);
            return ("【券市场】店员工号无效，请核实工号是否正确或是否是" + supplier.fullName + "门店。如有疑问请致电：400-6262-166");
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
                SMSUtil.send("【券市场】您的券号已过期，无法进行消费。如有疑问请致电：400-6262-166", mobile, code);
                return ("【券市场】您的券号已过期，无法进行消费。如有疑问请致电：400-6262-166");
            } else if (ecoupon.status == ECouponStatus.UNCONSUMED) {
                ecoupon.consumeAndPayCommission(shopId, supplierUser, VerifyCouponType.CONSUMER_MESSAGE);
                String coupon = ecoupon.getLastCode(4);
                String dateTime = DateUtil.getNowTime();
                // 发给店员
                SMSUtil.send("【券市场】" + getMaskedMobile(mobile) + "消费者的尾号" + coupon + "券（面值" + ecoupon
                        .faceValue + "元）于" + dateTime + "验证成功，门店：" + shopName + "。咨询4006262166", supplierUser.mobile, code);
                // 发给消费者
                SMSUtil.send("【券市场】您尾号" + coupon + "的券号于" + dateTime
                        + "已成功消费，使用门店：" + shopName + "。如有疑问请致电：400-6262-166", mobile, code);

                return ("【券市场】您尾号" + coupon + "的券号于" + dateTime
                        + "已成功消费，使用门店：" + shopName + "。如有疑问请致电：400-6262-166");
            } else if (ecoupon.status == ECouponStatus.CONSUMED) {
                // 发给消费者
                SMSUtil.send("【券市场】您的券号已消费，无法再次消费。如有疑问请致电：400-6262-166", mobile, code);
                return ("【券市场】您的券号已消费，无法再次消费。如有疑问请致电：400-6262-166");
            }
        }

        SMSUtil.send("【券市场】您的券号不能在" + shopName + "消费，请与店员确认。如有疑问请致电：400-6262-166", mobile, code);
        return ("【券市场】您的券号不能在" + shopName + "消费，mobile:" + mobile);
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
}
