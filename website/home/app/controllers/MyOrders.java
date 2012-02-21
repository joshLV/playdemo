package controllers;

import java.util.List;

import models.consumer.User;
import models.order.Orders;
import play.mvc.Controller;

public class MyOrders extends Controller {

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
	public static void details() {
		render();
	}

}
