package controllers;

import controllers.modules.website.cas.OAuthType;
import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.TargetOAuth;
import models.accounts.AccountType;
import models.consumer.User;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.sales.Shop;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Collection;
import java.util.List;

/**
 * User: yan
 * Date: 13-4-1
 * Time: 上午9:57
 */
@With({SecureCAS.class})
@TargetOAuth(OAuthType.SINA)
public class WebUserSinaVouchers extends Controller {
    public static final int TOP_NUMBER = 5;

    /**
     * 我的券
     */
    public static void myCoupons() {
        User user = SecureCAS.getUser();
        List<ECoupon> couponList = ECoupon.findWapCoupons(user, TOP_NUMBER);
        render(user, couponList);
    }

    /**
     * 查看更多券
     */
    public static void showMoreCoupon() {
        User user = SecureCAS.getUser();
        List<ECoupon> couponList = ECoupon.findWapCoupons(user, -1);
        render("WebUserSinaVouchers/myCoupons.html", user, couponList);
    }

    /**
     * 券详情
     */
    public static void showDetail(Long couponId) {
        User user = SecureCAS.getUser();
        ECoupon coupon = ECoupon.getCouponByIdAndUser(couponId, user);
        if (coupon == null) {
            error(404, "no coupon!");
            return;
        }
        Collection<Shop> shops = coupon.goods.getShopList();
        render(user, coupon, shops);
    }

    /**
     * 申请退款
     */
    public static void showRefund(Long couponId) {
        User user = SecureCAS.getUser();
        ECoupon coupon = ECoupon.getCouponByIdAndUser(couponId, user);
        if (coupon == null) {
            error(404, "no coupon!");
            return;
        }
        render(coupon);
    }

    /**
     * 退款
     */
    public static void refund(Long couponId) {

    }
}
