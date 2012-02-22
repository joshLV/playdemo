package controllers;

import java.util.List;

import models.consumer.User;
import models.order.OrderItems;
import models.order.Orders;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.cas.SecureCAS;
import controllers.modules.webcas.WebCAS;

@With({SecureCAS.class, WebCAS.class})
public class UserCenter extends Controller {

    /**
     * 我的订单
     */
    public static void index() {
        User user = WebCAS.getUser();
        List<Orders> orderList = Orders.find("byUser", user).fetch();
        render(orderList);
    }

    /**
     * 订单详情
     */
    public static void details(Long id) {
        //订单信息
        models.order.Orders     orders= models.order.Orders.findById(id);
        List<OrderItems> orderItems= orders.orderItems;
        //收货信息
        render(orders,orderItems);
    }

    /**
     * 订单详情
     */
    public static void tickets(String createdAt,String status) {
        User user = WebCAS.getUser();
        List<Orders> ticketList = Orders.userTicketsQuery(user.id,createdAt,status);
        render(ticketList);
    }

}
