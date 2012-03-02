package controllers;


import java.util.ArrayList;
import java.util.List;

import models.order.OrderItems;
import models.sales.Goods;

import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.cas.SecureCAS;

@With(SecureCAS.class)
public class Orders extends Controller {

    /**
     * 商户订单信息一览
     */
    public static void index(models.order.Orders orders) {
        List orderList= models.order.Orders.query(orders);
        render(orderList);

    }

    /**
     * 商户订单详细
     */
    public static void details(Long id) {

        //订单信息
        models.order.Orders orders= models.order.Orders.findById(Long.parseLong("1"));
         List<OrderItems> orderItems= orders.orderItems;
        //收货信息
        render(orders,orderItems);
    }

    /**
     * 券号列表
     */
    public static void tickets() {
        List ticketList= models.order.Orders.queryQ();
        render(ticketList);
    }
}