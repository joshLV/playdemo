package controllers;


import java.util.List;

import models.order.OrderItems;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.cas.SecureCAS;

@With(SecureCAS.class)
public class Orders extends Controller {

    /**
     * 商户订单信息一览
     * @param orders 页面信息
     */
	public static void index(models.order.Orders orders) {
        //该商户ID
		Long compnayId=1l;
		List orderList= models.order.Orders.query(orders,compnayId);
		render(orderList);
		
	}

    /**
     * 商户订单详细
     * @param id 订单ID
     */
	public static void details(Long id) {
		//订单信息
		models.order.Orders	orders= models.order.Orders.findById(id);
		 List<OrderItems> orderItems= orders.orderItems;
		//收货信息
		render(orders,orderItems);
	}

    /**
     * 券号列表
     */
	public static void coupons() {
		List couponsList= models.order.Orders.queryCoupons();
		render("Orders/e_coupons.html",couponsList);
	}

}