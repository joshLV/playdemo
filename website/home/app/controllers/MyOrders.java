package controllers;

import controllers.modules.cas.SecureCAS;
import controllers.modules.webcas.WebCAS;
import models.accounts.RefundBill;
import models.accounts.TradeBill;
import models.accounts.TradeStatus;
import models.accounts.util.RefundUtil;
import models.consumer.User;
import models.order.*;
import play.modules.breadcrumbs.BreadcrumbList;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@With({SecureCAS.class, WebCAS.class})
public class MyOrders extends Controller {

    /**
     * 我的订单
     */
    public static void index(Date createdAtBegin, Date createdAtEnd, OrderStatus status, String goodsName) {
        User user = WebCAS.getUser();
        List<Orders> orderList = Orders.findMyOrders(user, createdAtBegin, createdAtEnd, status, goodsName);

        BreadcrumbList breadcrumbs = new BreadcrumbList("我的订单", "/orders");
        render(orderList, breadcrumbs);
    }

    /**
     * 付款
     */
    public static void pay(Long id) {
        redirect("localhost:9001/payment_info/" + id);
    }

    /**
     * 订单详情
     */
    public static void details(Long id) {
        //订单信息
        models.order.Orders orders = models.order.Orders.findById(id);
        List<OrderItems> orderItems = orders.orderItems;
        //收货信息
        BreadcrumbList breadcrumbs = new BreadcrumbList("我的订单", "/orders", "订单详情", "/orders/" + id);
        render(orders, orderItems, breadcrumbs);
    }

    /**
     * 我的券列表
     */
    public static void coupons(Date createdAtBegin, Date createdAtEnd, ECouponStatus status, String goodsName) {
        User user = WebCAS.getUser();
        List<ECoupon> couponsList = Orders.userCuponsQuery(user, createdAtBegin, createdAtEnd, status, goodsName);

        BreadcrumbList breadcrumbs = new BreadcrumbList("我的券订单", "/coupons");
        render("MyOrders/e_coupons.html", couponsList, breadcrumbs);
    }
    
    public static void applyRefund(Long id, String applyNote){

        User user = WebCAS.getUser();
        ECoupon eCoupon = ECoupon.findById(id);
        if(eCoupon == null || !eCoupon.order.user.getId().equals(user.getId())){
            renderJSON("{\"error\":\"no such eCoupon\"}");
            return;
        }
        if(!(eCoupon.status == ECouponStatus.UNCONSUMED || eCoupon.status == ECouponStatus.EXPIRED)){
            renderJSON("{\"error\":\"can not apply refund with this goods\"}");
            return;
        }

        //查找原订单信息
        Orders order = eCoupon.order;
        TradeBill tradeBill = null;
        OrderItems orderItem = null;

        if(order != null){
            System.out.println("orderid:" + order.getId());
        }
        if(order != null){
            tradeBill = TradeBill.find("byOrderIdAndTradeStatus", order.getId(), TradeStatus.SUCCESS).first();
            if(tradeBill != null){
                System.out.println("orderid:" + tradeBill.getId());
            }
            orderItem = OrderItems.find("byOrderAndGoods",order, eCoupon.goods).first();
            if(orderItem != null){
                System.out.println("orderid:" + orderItem.getId());
            }
        }
        if(order == null || tradeBill == null || orderItem == null){
            renderJSON("{\"error\":\"can not get the trade bill\"}");
            return;
        }

        //创建退款流程
        RefundBill refundBill = RefundUtil.create(tradeBill, order.getId(), orderItem.getId(),
                orderItem.salePrice, applyNote);
        RefundUtil.success(refundBill);

        //更改库存
        eCoupon.goods.baseSale += 1;
        eCoupon.goods.saleCount -= 1;
        eCoupon.goods.save();

        //更改订单状态
        eCoupon.status = ECouponStatus.REFUND;
        eCoupon.save();

        renderJSON("{\"error\":\"ok\"}");
    }

}
