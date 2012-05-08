package controllers;

import models.admin.SupplierUser;
import models.order.ECoupon;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import play.mvc.Controller;

public class SmsReceivers extends Controller {

    /**
     * mobiles=1391234567&msg=23223432432&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt=1319873904&code=1028
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
       
       ECoupon ecoupon = ECoupon.findByMobileAndCode(mobile, code);
       if (ecoupon == null) {
           renderText("Not Found the ecoupon");
       }
       
       Supplier supplier = Supplier.findById(ecoupon.goods.supplierId);
       SupplierUser supplierUser = SupplierUser.find("from SupplierUser where supplier.id=? and jobNumber=?", ecoupon.goods.supplierId, msg).first();
       
       if (supplierUser == null) {
           SMSUtil.send("没有找到对应的店员工号，请确认您的输入只包括工号，或是否是在" + supplier.fullName + "门店消费，谢谢！", mobile, code);
       }
       if (supplierUser.shop == null) {
           SMSUtil.send("指定店员" + supplierUser.userName + "无门店设置，请询问店员，谢谢！", mobile, code);
       }
       
       ecoupon.consumed(supplierUser.shop.id);
       
       SMSUtil.send("收到" + mobile + "的" + ecoupon.faceValue + "元费用，券号:" + ecoupon.eCouponSn, supplierUser.mobile, code);
       
       renderText("ok, coupon=" + ecoupon.eCouponSn);
    }
}
