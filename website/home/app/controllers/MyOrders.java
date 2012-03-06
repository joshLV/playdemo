package controllers;

import java.util.Date;
import java.util.List;

import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.order.Orders;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.cas.SecureCAS;
import controllers.modules.webcas.WebCAS;

@With({SecureCAS.class, WebCAS.class})
public class MyOrders extends Controller {

	/**
	 * 我的订单
	 */
	public static void index(Date createdAtBegin,Date createdAtEnd,OrderStatus status,String goodsName) {
		User user = WebCAS.getUser();
		List<Orders> orderList = Orders.findMyOrders(user,createdAtBegin,createdAtEnd,status,goodsName);
		render(orderList);
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
		models.order.Orders orders= models.order.Orders.findById(id);
		List<OrderItems> orderItems= orders.orderItems;
		//收货信息
		render(orders,orderItems);
	}

	/**
	 * 我的券列表
	 */
	public static void coupons(Date createdAtBegin,Date createdAtEnd,ECouponStatus status,String goodsName) {
		User user = WebCAS.getUser();
		List<ECoupon> couponsList = Orders.userCuponsQuery(user,createdAtBegin,createdAtEnd,status,goodsName);
		render("MyOrders/e_coupons.html",couponsList);
	}

}
