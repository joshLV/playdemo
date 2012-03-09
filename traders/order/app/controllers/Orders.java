package controllers;


import java.util.List;

import org.apache.commons.lang.StringUtils;

import models.order.OrderItems;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.cas.SecureCAS;

@With(SecureCAS.class)
public class Orders extends Controller {

	public static int PAGE_SIZE = 15;

	/**
	 * 商户订单信息一览
	 * @param orders 页面信息
	 */
	public static void index(models.order.Orders orders) {
		//该商户ID
		Long companyId=1l;
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		JPAExtPaginator<models.order.Orders> orderList= models.order.Orders.query(orders,companyId,pageNumber, PAGE_SIZE);
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
		Long companyId=1l;
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		JPAExtPaginator<models.order.ECoupon> couponsList= models.order.Orders.queryCoupons(companyId,pageNumber, PAGE_SIZE);
		render("Orders/e_coupons.html",couponsList);
	}

}