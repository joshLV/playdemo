package controllers;

import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.VerifyCouponType;
import models.sales.Shop;
import play.mvc.Controller;

import java.util.Date;

public class FoundationJobsApplication extends Controller {

    public static void index(String coupons) {
//        Long[] couponIds = {143295L, 143296L, 143297L, 143298L, 143299L, 143300L, 143301L, 143302L, 143303L};
       String[] couponIds=coupons.split(",");
        for (String id : couponIds) {
            ECoupon coupon = ECoupon.findById(Long.parseLong(id));
            if (coupon != null) {
                System.out.println(coupon + "-----");
                Shop shop = Shop.findById(3580L);
                SupplierUser supplierUser = SupplierUser.find("byShop", shop).first();
                coupon.payCommission();
                coupon.shop = shop;
                coupon.partner = ECouponPartner.MT;
                coupon.consumedAt = new Date();
                coupon.supplierUser = supplierUser;
                coupon.save();
            }
        }

        render();
    }


}
