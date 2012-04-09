package controllers;

import java.util.Date;
import java.util.List;
import models.consumer.User;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import org.apache.commons.lang.StringUtils;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.website.cas.SecureCAS;

@With(SecureCAS.class)
public class MyOrders extends Controller {

	public static int PAGE_SIZE = 15;

	/**
	 * 我的订单
	 */
	public static void index(Date createdAtBegin, Date createdAtEnd, OrderStatus status, String goodsName) {
		User user = SecureCAS.getUser();
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

		JPAExtPaginator<models.order.Order>  orderList = Order.findMyOrders(user, createdAtBegin, createdAtEnd, status, goodsName,pageNumber, PAGE_SIZE);

		BreadcrumbList breadcrumbs = new BreadcrumbList("我的订单", "/orders");
		renderArgs.put("createdAtBegin", createdAtBegin);
		renderArgs.put("createdAtEnd", createdAtEnd);
		renderArgs.put("status", status);
		renderArgs.put("goodsName", goodsName);
		render(orderList, breadcrumbs);
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
	
}
