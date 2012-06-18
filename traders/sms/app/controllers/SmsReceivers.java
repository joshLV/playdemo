package controllers;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.FieldCheckUtil;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.Brand;
import models.sales.Shop;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
import play.mvc.Controller;

import java.util.Date;
import java.util.List;

public class SmsReceivers extends Controller {

    /**
     * 消费者验证：mobiles=1391234567&msg=9527&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt=1319873904&code=1028
     * 店员验证：  mobiles=15900002342&msg=#xxxx#&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt=1319873904&code=1028
     */
    public static void getSms() {
        String mobile = params.get("mobiles");
        String msg = params.get("msg");
        String username = params.get("username");
        String pwd = params.get("pwd");
        String dt = params.get("dt");
        String code = params.get("code");

        System.out.println("mobile=" + mobile + ",msg=" + msg + ",username="
                + username + ",pwd=" + pwd + ",dt=" + dt + ",code=" + code);

        if (FieldCheckUtil.isNumeric(msg)) {
            // 消费者验证的情况
            checkConsumer(mobile, msg, code);
        } else if (msg.contains("#")) {
            // 店员验证
            checkClerk(mobile, msg, code);
        } else {
            renderText("msg is wrong");

        }
    }

    /**
     * 店员验证的情况
     *
     * @param mobile 手机
     * @param msg    券号
     * @param code   回复代码
     */


    private static void checkClerk(String mobile, String msg, String code) {
        String[] couponArray = msg.split("#");

        //验证店员是否存在
        String couponNumber = "";
        int couponCount = couponArray.length;
        for (int i = 1; i < couponCount; i++) {
            couponNumber = couponArray[i];
            if (!FieldCheckUtil.isNumeric(couponNumber)) {
                SMSUtil.send("【券市场】您输入的券号" + couponNumber + "无效，请确认！", mobile, code);
                renderText("券号无效！");
            }

            ECoupon ecoupon = ECoupon.query(couponNumber, null);

            if (ecoupon == null) {
                SMSUtil.send("【券市场】您输入的券号" + couponNumber + "不存在，请确认！", mobile, code);
                renderText("【券市场】您输入的券号" + couponNumber + "不存在，请确认！");
            } else {
                Long supplierId = ecoupon.goods.supplierId;

                Supplier supplier = Supplier.findById(supplierId);

                if (supplier == null || supplier.deleted == DeletedStatus.DELETED) {
                    SMSUtil.send("【券市场】该商户不存在或被删除了！，请确认！", mobile, code);
                    renderText("【券市场】该商户不存在或被删除了！，请确认！");
                }

                if (supplier.status == SupplierStatus.FREEZE) {
                    SMSUtil.send("【券市场】该商户已被锁定，请确认！", mobile, code);
                    renderText("【券市场】该商户已被锁定，请确认！");
                }

                SupplierUser supplierUser = SupplierUser.findByMobileAndSupplier(mobile, supplier);

                List<Brand> brandList = Brand.findByOrder(supplier);

                if (supplierUser == null || !brandList.contains(ecoupon.goods.brand)) {
                    SMSUtil.send("【券市场】店员工号无效，请核实工号是否正确或是否是" + supplier.fullName + "门店。如有疑问请致电：400-6262-166", mobile, code);
                    renderText("【券市场】店员工号无效，请核实工号是否正确或是否是" + supplier.fullName + "门店。如有疑问请致电：400-6262-166");
                }

                Long shopId = supplierUser.shop.id;
                Shop shop = Shop.findById(shopId);
                String shopName = shop.name;

                if (ecoupon.status == ECouponStatus.UNCONSUMED) {
                    ecoupon.consumed(supplierUser.shop.id, supplierUser, VerifyCouponType.CLERK_MESSAGE);
                    String coupon = ecoupon.getMaskedEcouponSn();
                    coupon = coupon.substring(coupon.lastIndexOf("*") + 1);

                    String dateTime = DateUtil.getNowTime();

                    // 发给店员
                    SMSUtil.send("【券市场】," + getMaskedMobile(mobile) + "消费者的尾号" + coupon + "的券（面值：" + ecoupon
                            .faceValue + "元）于" + dateTime + "已验证成功，使用门店：" + shopName + "。客服热线：400" +
                            "-6262-166", supplierUser.mobile, code);
                    // 发给消费者
                    SMSUtil.send("【券市场】您尾号" + coupon + "的券号于" + dateTime
                            + "已成功消费，使用门店：" + shopName + "。如有疑问请致电：400-6262-166", mobile, code);
                } else if (ecoupon.status == ECouponStatus.CONSUMED) {
                    SMSUtil.send("【券市场】您的券号已消费，无法再次消费。如有疑问请致电：400-6262-166", mobile, code);
                } else if (ecoupon.expireAt.before(new Date())) {
                    SMSUtil.send("【券市场】您的券号已过期，无法进行消费。如有疑问请致电：400-6262-166", mobile, code);
                }
            }
        }
        if (couponCount == 0) {
            renderText("券号无效！");
        }


    }

    /**
     * 消费者验证的情况
     *
     * @param mobile 手机
     * @param msg    工号
     * @param code   回复代码
     */
    private static void checkConsumer(String mobile, String msg, String code) {
        // 消费者验证逻辑
        ECoupon ecoupon = ECoupon.findByMobileAndCode(mobile, code);
        if (ecoupon == null) {
            renderText("Not Found the coupon");
        }

        Supplier supplier = Supplier.findById(ecoupon.goods.supplierId);
        SupplierUser supplierUser = SupplierUser.find("from SupplierUser where supplier.id=? and jobNumber=?", ecoupon.goods.supplierId, msg).first();

        if (supplierUser == null) {
            // 发给消费者
            SMSUtil.send("【券市场】店员工号无效，请核实工号是否正确或是否是" + supplier.fullName + "门店。如有疑问请致电：400-6262-166",
                    mobile, code);
            renderText("【券市场】店员工号无效，请核实工号是否正确或是否是" + supplier.fullName + "门店。如有疑问请致电：400-6262-166");
        }

        boolean isExisted = false;
        Long shopId = supplierUser.shop.id;

        //判断该券是否属于所在消费门店
        if (!ecoupon.goods.isAllShop) {
            int cnt = 0;
            for (Shop shop : ecoupon.goods.shops) {
                if (shop.id.compareTo(shopId) == 0) {
                    cnt++;
                }
            }
            if (cnt == 0) {
                isExisted = true;

            }
        }

        //取得消费门店
        Shop shop = Shop.findById(shopId);
        String shopName = shop.name;
        //门店不在
        if (isExisted) {
            SMSUtil.send("【券市场】您的券号不能在" + shopName + "消费，请与店员确认。如有疑问请致电：400-6262-166", mobile, code);
            renderText("【券市场】您的券号不能在" + shopName + "消费，mobile:" + mobile);
        }

        if (ecoupon.status == ECouponStatus.UNCONSUMED) {
            ecoupon.consumed(supplierUser.shop.id, supplierUser,VerifyCouponType.CONSUMER_MESSAGE);
            String coupon = ecoupon.getLastCode(4);
            String dateTime = DateUtil.getNowTime();
            // 发给店员
            SMSUtil.send("【券市场】," + getMaskedMobile(mobile) + "消费者的尾号" + coupon + "的券（面值：" + ecoupon
                    .faceValue + "元）于" + dateTime + "已验证成功，使用门店：" + shopName + "。客服热线：400" +
                    "-6262-166", supplierUser.mobile, code);
            // 发给消费者
            SMSUtil.send("【券市场】您尾号" + coupon + "的券号于" + dateTime
                    + "已成功消费，使用门店：" + shopName + "。如有疑问请致电：400-6262-166", mobile, code);

        } else if (ecoupon.status == ECouponStatus.CONSUMED) {
            // 发给消费者
            SMSUtil.send("【券市场】您的券号已消费，无法再次消费。如有疑问请致电：400-6262-166", mobile, code);
        } else if (ecoupon.expireAt.before(new Date())) {
            //过期
            SMSUtil.send("【券市场】您的券号已过期，无法进行消费。如有疑问请致电：400-6262-166", mobile, code);
        }
    }

    /**
     * 处理消费者手机号
     *
     * @param mobile 手机号
     * @return 处理后的手机号
     */
    private static String getMaskedMobile(String mobile) {
        StringBuilder sn = new StringBuilder();
        sn.append(mobile.substring(0, 3) + "*****" + mobile.substring(8, 11));
        return sn.toString();

    }
}
