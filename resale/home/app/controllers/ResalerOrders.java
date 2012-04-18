package controllers;

import java.util.List;
import java.util.Map;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrdersCondition;
import models.resale.Resaler;
import org.apache.commons.lang.StringUtils;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.resale.cas.SecureCAS;

/**
 * 分销商订单列表控制器
 *
 * @author yanjy
 *
 */
@With(SecureCAS.class)
public class ResalerOrders extends Controller {

	public static int PAGE_SIZE = 6;
	public static int LIMIT = 8;

	/**
	 * 订单页面展示
	 */
	public static void index(OrdersCondition condition) {
		Resaler resaler = SecureCAS.getResaler();
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		if (condition == null) {
    		condition = new OrdersCondition();
    	}
		JPAExtPaginator<models.order.Order>  orderList = Order.findResalerOrders(condition,resaler,pageNumber, PAGE_SIZE);

		BreadcrumbList breadcrumbs = new BreadcrumbList("我的订单", "/orders");
		renderGoodsCond(condition);
		
		//取得本月和上月订单笔数,总金额
		Order.getThisMonthTotal(resaler);
		Map totalMap = Order.getTotalMap();
		
		render(orderList, breadcrumbs,totalMap,resaler);
	}

	/**
	 * 订单详情
	 * @param id 订单ID
	 */
	public static void show(long id) {
		//订单信息
		models.order.Order order = models.order.Order.findById(id);
		List<OrderItems> orderItems = order.orderItems;
		//收货信息
		BreadcrumbList breadcrumbs = new BreadcrumbList("我的订单", "/orders", "订单详情", "/orders/" + id);
		render(order, orderItems,breadcrumbs);
	}

	/**
	 * 付款
	 */
	public static void pay(Long id) {
		redirect("/payment_info/" + id);
	}
	
	/**
	 * 向页面设置选择信息
	 * 
	 * @param goodsCond 页面设置选择信息
	 */
	private static void renderGoodsCond(OrdersCondition goodsCond) {
		renderArgs.put("createdAtBegin", goodsCond.createdAtBegin);
		renderArgs.put("createdAtEnd", goodsCond.createdAtEnd);
		renderArgs.put("status", goodsCond.status);
		renderArgs.put("goodsName", goodsCond.goodsName);
	}
}
