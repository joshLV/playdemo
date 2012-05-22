package controllers;

import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import play.mvc.Controller;

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


       // 消费者验证逻辑
       ECoupon ecoupon = ECoupon.findByMobileAndCode(mobile, code);
       if (ecoupon == null) {
           renderText("Not Found the ecoupon");
       }

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
