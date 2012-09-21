package controllers;

import com.uhuila.common.constants.DeletedStatus;
import controllers.modules.website.cas.SecureCAS;
import models.accounts.Account;
import models.accounts.CashCoupon;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.consumer.User;
import play.cache.Cache;
import play.libs.Codec;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;

/**
 * @author likang
 *         Date: 12-9-20
 */
@With({SecureCAS.class, WebsiteInjector.class})
public class CashCoupons extends Controller{
    public static void index(){
        String randomID = Codec.UUID();
        render(randomID);
    }

    public static void charge(String couponCode, String code, String randomID){
        CashCoupon coupon = CashCoupon.find("byChargeCode", couponCode).first();
        String errMsg = null;
        if (coupon == null){
            errMsg = "找不到该代金券";
        }else if(coupon.chargedAt != null){
            errMsg = "该券已被使用";
        }else if(coupon.deleted == DeletedStatus.DELETED){
            errMsg = "该券无法使用";
        }else if(!Cache.get(randomID).equals(code)){
            errMsg = "验证码错误";
        }

        if(errMsg == null){
            User user = SecureCAS.getUser();

            coupon.chargedAt = new Date();
            coupon.userId = user.getId();
            coupon.save();

            Account account = AccountUtil.getConsumerAccount(user.getId());
            TradeBill tradeBill = TradeUtil.createPromotionChargeTrade(account, coupon.faceValue, null);
            TradeUtil.success(tradeBill, "代金券: " + coupon.name + " 充值" + coupon.faceValue + "元");
        }

        Cache.delete(randomID);
        randomID = Codec.UUID();
        render("CashCoupons/index.html",randomID, errMsg);
    }

}
