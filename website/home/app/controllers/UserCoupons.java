package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.accounts.AccountType;
import models.consumer.User;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.ECouponHistoryMessage;
import models.sales.Shop;
import org.apache.commons.lang.StringUtils;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Collection;

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
        render(shops);
    }


    /**
     * 申请退款
     *
     * @param id        券ID
     * @param applyNote 退款原因
     */
    public static void applyRefund(Long id, String applyNote) {
        System.out.println("id:" + id);
        System.out.println("applyNote:" + applyNote);
        User user = SecureCAS.getUser();
        ECoupon eCoupon = ECoupon.findById(id);
        System.out.println("eCoupon:" + eCoupon);
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
        boolean sendFalg = ECoupon.sendUserMessageInfo(id,couponshopsId);
        ECouponHistoryMessage.with(eCoupon).operator(user.getShowName())
                .remark("重发短信").sendToMQ();
        renderJSON(sendFalg ? "0" : "1");
    }
}
