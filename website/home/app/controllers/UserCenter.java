package controllers;

import java.util.List;

import models.consumer.User;
import models.order.OrderItems;
import models.order.Orders;
import play.mvc.Controller;

public class UserCenter extends Controller {

	/**
	 * 我的订单
	 */
	public static void index() {
		String username = session.get("username");
		username = "yjy";
		User user = User.find("byLoginName", username).first();
		List<Orders> orderList = Orders.find("byUser", user).fetch();
		render(orderList);
	}

	/**
	 * 订单详情
	 */
	public static void details(Long id) {
		//订单信息
		models.order.Orders	orders= models.order.Orders.findById(id);
		 List<OrderItems> orderItems= orders.orderItems;
		//收货信息
		render(orders,orderItems);
	}

	/**
	 * 订单详情
	 */
	public static void tickets(String createdAt,String status) {
		String username = session.get("username");
		username = "yjy";
		User user = User.find("byLoginName", username).first();
		List<Orders> ticketList = Orders.userTicketsQuery(user.id,createdAt,status);
		render(ticketList);
	}

}
