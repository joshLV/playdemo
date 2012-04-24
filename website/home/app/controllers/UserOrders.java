package controllers;

import java.util.List;

import models.consumer.User;
import models.order.CouponsCondition;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrdersCondition;

import org.apache.commons.lang.StringUtils;

import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.website.cas.SecureCAS;

@With(SecureCAS.class)
public class UserOrders extends Controller {

	public static int PAGE_SIZE = 15;

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
		JPAExtPaginator<models.order.Order>  orderList = Order.findUserOrders(user, condition,pageNumber, PAGE_SIZE);

		BreadcrumbList breadcrumbs = new BreadcrumbList("我的订单", "/orders");
		renderCond(condition);
		render(orderList, breadcrumbs,user);
	}

	/**
	 * 付款
	 */
	public static void pay(Long id) {
		redirect("http://www.uhuila.cn/payment_info/" + id);
	}

	/**
	 * 订单详情
	 */
	public static void details(Long id) {
		//订单信息
		models.order.Order order = models.order.Order.findById(id);
		List<OrderItems> orderItems = order.orderItems;
		//收货信息
		BreadcrumbList breadcrumbs = new BreadcrumbList("我的订单", "/orders", "订单详情", "/orders/" + id);
		render(order, orderItems,breadcrumbs);
	}

	/**
	 * 向页面设置选择信息
	 * 
	 * @param condition 页面设置选择信息
	 */
	private static void renderCond(OrdersCondition condition) {
		renderArgs.put("createdAtBegin", condition.createdAtBegin);
		renderArgs.put("createdAtEnd", condition.createdAtEnd);
		renderArgs.put("status", condition.status);
		renderArgs.put("goodsName", condition.goodsName);
	}

}
