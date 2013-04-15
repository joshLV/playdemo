package controllers;

import controllers.modules.resale.cas.SecureCAS;
import models.accounts.AccountType;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrdersCondition;
import models.resale.Resaler;
import org.apache.commons.lang.StringUtils;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 分销商订单列表控制器
 *
 * @author yanjy
 */
@With(SecureCAS.class)
public class ResalerOrders extends Controller {

    public static int PAGE_SIZE = 6;
    public static int LIMIT = 8;

    /**
     * 订单页面展示
     */
    public static void index(OrdersCondition condition) {
        Resaler resaler = SecureCAS.getResaler();
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new OrdersCondition();
        }
        JPAExtPaginator<models.order.Order> orderList = Order.findResalerOrders(condition, resaler, pageNumber, PAGE_SIZE);
        BreadcrumbList breadcrumbs = new BreadcrumbList("我的订单", "/orders");

        render(orderList, breadcrumbs,resaler,condition);
    }

    /**
     * 订单详情
     *
     * @param orderNumber 订单编号
     */
    public static void show(String orderNumber) {
        Resaler resaler = SecureCAS.getResaler();
        //订单信息
        models.order.Order order = models.order.Order.findOneByResaler(orderNumber, resaler.getId(), AccountType.RESALER);
        List<ECoupon> eCoupons = ECoupon.findByOrder(order);
        //收货信息
        BreadcrumbList breadcrumbs = new BreadcrumbList("我的订单", "/orders", "订单详情", "/orders/" + orderNumber);
        render(order, eCoupons, breadcrumbs);
    }

    public static void batchRefund(List<Long> couponIds, String orderNumber) {
        Resaler resaler = SecureCAS.getResaler();
        if (couponIds == null || couponIds.size() == 0) {
            show(orderNumber);
        }
        List<ECoupon> eCoupons = ECoupon.findByUserAndIds(couponIds, resaler.getId(), AccountType.RESALER);
        for (ECoupon eCoupon : eCoupons) {
            ECoupon.applyRefund(eCoupon);
        }
        show(orderNumber);
    }

    /**
     * 订单关闭
     */
    public static void cancelOrder(String orderNumber) {
        //加载用户账户信息
        Resaler resaler = SecureCAS.getResaler();
        //订单信息
        models.order.Order order = models.order.Order.findOneByResaler(orderNumber, resaler.getId(), AccountType.RESALER);
        //更新订单信息
        order.cancelAndUpdateOrder();
        renderJSON("");
    }

    /**
     * 付款
     */
    public static void pay(String orderNumber) {
        redirect("/payment_info/" + orderNumber);
    }

}
