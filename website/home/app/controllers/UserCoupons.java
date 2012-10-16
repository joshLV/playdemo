package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.accounts.AccountType;
import models.consumer.User;
import models.order.CouponHistory;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.sales.Shop;
import org.apache.commons.lang.StringUtils;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@With({SecureCAS.class, WebsiteInjector.class})
public class UserCoupons extends Controller {

    public static int PAGE_SIZE = 15;

    /**
     * 我的券列表
     */
    public static void index(CouponsCondition condition) {
        User user = SecureCAS.getUser();
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new CouponsCondition();
        }
        condition.userId = user.id;
        condition.accountType = AccountType.CONSUMER;
        JPAExtPaginator<ECoupon> couponsList = ECoupon.getUserCoupons(condition, pageNumber, PAGE_SIZE);
        BreadcrumbList breadcrumbs = new BreadcrumbList("我的券", "/coupons");
        render(couponsList, breadcrumbs, user, condition);
    }

    public static void showCouponShops(Long id) {
        ECoupon couponItem = null;
        Collection<Shop> shops = null;
        if (id != null) {
            couponItem = ECoupon.findById(id);
            shops = couponItem.orderItems.goods.getShopList();
        }
        System.out.println("couponItem>>>" + couponItem);
        System.out.println("shop>>>" + shops);
        render(shops);
    }


    /**
     * 申请退款
     *
     * @param id        券ID
     * @param applyNote 退款原因
     */
    public static void applyRefund(Long id, String applyNote) {
        User user = SecureCAS.getUser();
        ECoupon eCoupon = ECoupon.findById(id);
        if (eCoupon == null) {
            error(404, "no coupon!");
            return;
        }
        String returnFlg = ECoupon.applyRefund(eCoupon, user.getId(), AccountType.CONSUMER);
        renderJSON(returnFlg);
    }


    /**
     * 重发短信
     *
     * @param id
     */
    public static void sendMessage(long id, String couponshopsId) {
        User user = SecureCAS.getUser();
        ECoupon eCoupon = ECoupon.findById(id);
        boolean sendFalg = ECoupon.sendUserMessage(id);
        if (StringUtils.isNotBlank(couponshopsId))
            sendFalg = ECoupon.sendUserShopsInfoMessage(id, couponshopsId);
        new CouponHistory(eCoupon, user.getShowName(), "重发短信", eCoupon.status, eCoupon.status, null).save();
        renderJSON(sendFalg ? "0" : "1");
    }

    public static void showGoodsShops(Long supplierId) {
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        render(shopList);
    }
}
