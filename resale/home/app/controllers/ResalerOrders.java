package controllers;

import java.util.List;
import java.util.Map;

import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.resale.Resaler;
import models.resale.ResalerOrdersCondition;
import models.resale.util.ResaleUtil;

import org.apache.commons.lang.StringUtils;

import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.cas.SecureCAS;
import controllers.resaletrace.ResaleCAS;

/**
 * 分销商订单列表控制器
 *
 * @author yanjy
 *
 */
@With({SecureCAS.class,ResaleCAS.class})
public class ResalerOrders extends Controller {

	public static int PAGE_SIZE = 6;
	public static int LIMIT = 8;

	/**
	 * 订单页面展示
	 */
	public static void index(ResalerOrdersCondition condition) {
		Resaler resaler = ResaleCAS.getResaler();
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		if (condition == null) {
    		condition = new ResalerOrdersCondition();
    	}
		JPAExtPaginator<models.order.Order>  orderList = Order.findResalerOrders(condition,resaler,pageNumber, PAGE_SIZE);

		BreadcrumbList breadcrumbs = new BreadcrumbList("我的订单", "/orders");
		renderGoodsCond(condition);
		
		
		//取得本月订单总金额
		Map thisMonthMap = ResaleUtil.findThisMonth();
		Map lastMonthMap = ResaleUtil.findLastMonth();
		long instantTotal = Order.getThisMonthTotal(resaler,lastMonthMap,thisMonthMap,OrderStatus.PAID);
		//取得上月订单信息
		render(orderList, breadcrumbs);
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
		redirect("http://www.uhuila.cn/payment_info/" + id);
	}
	
	/**
	 * 向页面设置选择信息
	 * 
	 * @param goodsCond 页面设置选择信息
	 */
	private static void renderGoodsCond(ResalerOrdersCondition goodsCond) {
		renderArgs.put("createdAtBegin", goodsCond.createdAtBegin);
		renderArgs.put("createdAtEnd", goodsCond.createdAtEnd);
		renderArgs.put("status", goodsCond.status);
		renderArgs.put("goodsName", goodsCond.goodsName);
	}
}
