package controllers;


import controllers.supplier.cas.SecureCAS;
import models.order.ECoupon;
import models.order.OrderItems;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

@With({SecureCAS.class, MenuInjector.class})
@ActiveNavigation("order_index")
public class SupplierOrders extends Controller {

	public static int PAGE_SIZE = 15;

	/**
	 * 商户订单信息一览
	 *
	 * @param orders 页面信息
	 */
	public static void index(models.order.Order orders) {
		//该商户ID
		Long supplierId = MenuInjector.currentUser().supplier.getId();
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		JPAExtPaginator<models.order.Order> orderList = models.order.Order.query(orders, supplierId, pageNumber, PAGE_SIZE);
		renderArgs.put("order.createdAtBegin", orders.createdAtBegin);
		render(orderList);

	}

	/**
	 * 商户订单详细
	 *
	 * @param id 订单ID
	 */
	public static void details(Long id) {
		//订单信息
		models.order.Order orders = models.order.Order.findById(id);
		List<OrderItems> orderItems = orders.orderItems;
		//收货信息
		render(orders, orderItems);
	}

	/**
	 * 券号列表
	 */
	public static void coupons() {
		Long supplierId = MenuInjector.currentUser().supplier.getId();
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		JPAExtPaginator<models.order.ECoupon> couponsList = ECoupon.queryCoupons(supplierId, pageNumber, PAGE_SIZE);
		render("SupplierOrders/e_coupons.html", couponsList);
	}

}
