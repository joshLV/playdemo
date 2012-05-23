package controllers;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.FieldCheckUtil;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.sales.Brand;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
import play.mvc.Controller;

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
        } else if (FieldCheckUtil.isNumericRule(msg)) {
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
        //验证店员是否存在
        String couponNumber = msg.replaceAll("#", "");
        ECoupon ecoupon = ECoupon.query(couponNumber, null);

        if (ecoupon == null) {
            renderText("Not Found the coupon");
        } else {
            Long supplierId = ecoupon.goods.supplierId;

            Supplier supplier = Supplier.findById(supplierId);

            if (supplier == null || supplier.deleted == DeletedStatus.DELETED) {
                renderText("Not Found the supplier");
            }

            if (supplier.status == SupplierStatus.FREEZE) {
                renderText("The supplier was freeze!");
            }

            SupplierUser supplierUser = SupplierUser.findByMobileAndSupplier(mobile, supplier);

            if (supplierUser == null) {
                SMSUtil.send("请核对你的信息,重新发送短信！", mobile, code);
                SMSUtil.send("没有找到对应的店员信息，请确认您是否商户<" + supplier.fullName + ">的店员，谢谢！", mobile, code);
                renderText("该商户下没有此店员信息！");
            }
            List<Brand> brandList = Brand.findByOrder(supplier);

            if (!brandList.contains(ecoupon.goods.brand)) {
                SMSUtil.send("对不起，该券不是该商户品牌下的，请确认！", mobile, code);
                renderText("对不起，该券不是该商户品牌的，请确认！");
            }
            if (supplierUser.shop == null) {
                // 发给店员
                SMSUtil.send("商户<" + supplier.fullName + ">无门店设置，请确认，谢谢！", mobile, code);
                renderText("无门店设置，请确认，谢谢！");
            }

            if (ecoupon.status == ECouponStatus.UNCONSUMED) {
                ecoupon.consumed(supplierUser.shop.id);
                // 发给店员
                SMSUtil.send("券号:" + ecoupon.getMaskedEcouponSn() + ",(面值" + ecoupon.faceValue + "元)消费成功", supplierUser.mobile, code);
                // 发给消费者
                SMSUtil.send("你的券号:" + ecoupon.getMaskedEcouponSn() + "(面值" + ecoupon.faceValue + "元)已经消费成功", mobile, code);
            } else {
                // 发给消费者
                SMSUtil.send("你的券号:" + ecoupon.getMaskedEcouponSn() + "(面值" + ecoupon.faceValue + "元)不能消费，状态码：" + ecoupon.status.toString(), mobile, code);


            }
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

        System.out.println("ecoupon.goods.supplierId========="+ecoupon.goods.supplierId);
        Supplier supplier = Supplier.findById(ecoupon.goods.supplierId);
        SupplierUser supplierUser = SupplierUser.find("from SupplierUser where supplier.id=? and jobNumber=?", ecoupon.goods.supplierId, msg).first();

        if (supplierUser == null) {
            // 发给消费者
            SMSUtil.send("没有找到对应的店员工号，请确认您的输入只包括工号，或确认您是在<" + supplier.fullName + ">的门店消费，谢谢！", mobile, code);
            renderText("error, coupon=" + ecoupon.getMaskedEcouponSn() + ",没有找到对应的店员工号");
        }
        if (supplierUser.shop == null) {
            // 发给消费者
            SMSUtil.send("指定店员" + supplierUser.userName + "无门店设置，请询问店员，谢谢！", mobile, code);
            renderText("error, coupon=" + ecoupon.getMaskedEcouponSn() + ",指定店员" + supplierUser.userName + "无门店设置");
        }

        if (ecoupon.status == ECouponStatus.UNCONSUMED) {
            ecoupon.consumed(supplierUser.shop.id);
            // 发给店员
            SMSUtil.send("收到" + mobile + "的" + ecoupon.faceValue + "元费用，券号:" + ecoupon.getMaskedEcouponSn(), supplierUser.mobile, code);
            // 发给消费者
            SMSUtil.send("你的券号:" + ecoupon.getMaskedEcouponSn() + "(面值" + ecoupon.faceValue + "元)已经消费成功", mobile, code);
        } else {
            // 发给消费者
            SMSUtil.send("你的券号:" + ecoupon.getMaskedEcouponSn() + "(面值" + ecoupon.faceValue + "元)不能消费，状态码：" + ecoupon.status.toString(), mobile, code);
        }

        renderText("ok, coupon=" + ecoupon.getMaskedEcouponSn());
    }
}
