package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.accounts.AccountType;
import models.accounts.Voucher;
import models.consumer.User;
import models.order.*;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

@With({SecureCAS.class, WebsiteInjector.class})
public class UserOrders extends Controller {
    public static final String BASE_DEMAIN = Play.configuration.getProperty("application.baseDomain");

    public static int PAGE_SIZE = 15;

    public static void goIndex() {
        index(null);
    }

    /**
     * 我的订单
     */
    public static void index(OrdersCondition condition) {
        User user = SecureCAS.getUser();
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new OrdersCondition();
        }

        JPAExtPaginator<models.order.Order> orderList = Order.findUserOrders(user, condition, pageNumber, PAGE_SIZE);

        BreadcrumbList breadcrumbs = new BreadcrumbList("我的订单", "/orders");
        render(orderList, breadcrumbs, user, condition);
    }

    /**
     * 付款
     */
    public static void pay(String orderNumber) {
        redirect("http://www." + BASE_DEMAIN + "/payment_info/" + orderNumber);
    }

    /**
     * 订单详情
     */
    public static void details(String orderNumber) {
        //加载用户账户信息
        User user = SecureCAS.getUser();

        //加载订单信息
        Order order = Order.findOneByUser(orderNumber, user);
        if (order == null) {
            error(404, "no order!");
            return;
        }
        List<OrderItems> orderItems = order.orderItems;

        //收货信息
        BreadcrumbList breadcrumbs = new BreadcrumbList("我的订单", "/orders", "订单详情", "/orders/" + orderNumber);
        render(order, orderItems, breadcrumbs);
    }


    /**
     * 显示要退款的订单信息
     */
    public static void refund(String orderNumber) {
        //加载用户账户信息
        User user = SecureCAS.getUser();

        //加载订单信息
        Order order = Order.findOneByUser(orderNumber, user);
        if (order == null) {
            error(404, "no order!");
            return;
        }
        List<ECoupon> eCoupons = ECoupon.findByOrder(order);
        //收货信息
        BreadcrumbList breadcrumbs = new BreadcrumbList("我的订单", "/orders", "申请退款", "/orders/refund/" + orderNumber);
        render(order, eCoupons, breadcrumbs);
    }

    /**
     * 申请退款
     *
     * @param couponIds   ids
     * @param orderNumber 订单编号
     */
    public static void batchRefund(List<Long> couponIds, String orderNumber) {
        User user = SecureCAS.getUser();
        if (couponIds == null || couponIds.size() == 0) {
            refund(orderNumber);
        }

        List<ECoupon> eCoupons = ECoupon.findByUserAndIds(couponIds, user.getId(), AccountType.CONSUMER);
        for (ECoupon eCoupon : eCoupons) {
            ECoupon.applyRefund(eCoupon);
        }
        refund(orderNumber);
    }

    /**
     * 订单关闭
     */
    public static void cancelOrder(String orderNumber) {
        //加载用户账户信息
        User user = SecureCAS.getUser();
        //加载订单信息
        Order order = Order.findOneByUser(orderNumber, user);
        if (order == null) {
            error(404, "no order!");
            return;
        }
        //更新订单信息
        order.cancelAndUpdateOrder();
        List<Voucher> voucherList = Voucher.find("byOrder", order).fetch();
        for(Voucher voucher : voucherList) {
            voucher.order = null;
            voucher.usedAt = null;
            voucher.save();
        }
        renderJSON("");
    }

}
