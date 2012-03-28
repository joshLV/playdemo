package controllers;

import java.sql.SQLOutput;
import java.util.Map;

import models.order.ECoupon;
import navigation.annotations.ActiveNavigation;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;
import controllers.supplier.cas.SecureCAS;


@With({SecureCAS.class, MenuInjector.class})
@ActiveNavigation("coupon_verify")
public class VerificationCoupons extends Controller {

    /**
     * 验证页面
     */
    public static void index() {
        render("Verification/index.html");
    }

    /**
     * 查询
     *
     * @param eCouponSn 券号
     */
    public static void queryCoupons(String eCouponSn) {
        if (Validation.hasErrors()) {
            params.flash();
            Validation.keep();
            renderTemplate("Verification/index.html", eCouponSn);
        }
        
        Long supplierId = MenuInjector.currentUser().getId();
        //根据页面录入券号查询对应信息
        Map<String, Object> queryMap = ECoupon.queryInfo(eCouponSn, supplierId);
        renderJSON(queryMap);
    }

    /**
     * 修改券状态,并产生消费交易记录
     *
     * @param eCouponSn 券号
     */
    public static void update(String eCouponSn) {
        if (Validation.hasErrors()) {
            params.flash();
            Validation.keep();
            renderTemplate("Verification/index.html", eCouponSn);
        }
        Long supplierId = MenuInjector.currentUser().getId();
        ECoupon eCoupon = ECoupon.query(eCouponSn, supplierId);
        
        //根据页面录入券号查询对应信息,并产生消费交易记录
        if (eCoupon == null){
            renderJSON("0");
            return;
        }
        eCoupon.consumed();
        renderJSON("err");
    }
}
