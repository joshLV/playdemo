package controllers;

import controllers.modules.cas.SecureCAS;
import controllers.modules.webcas.WebCAS;
import models.accounts.RefundBill;
import models.accounts.TradeBill;
import models.accounts.TradeStatus;
import models.accounts.util.RefundUtil;
import models.consumer.User;
import models.order.*;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

@With({SecureCAS.class, WebCAS.class})
public class MyOrders extends Controller {

	public static int PAGE_SIZE = 15;

	/**
	 * 我的订单
	 */
	public static void index(Date createdAtBegin, Date createdAtEnd, OrderStatus status, String goodsName) {
		User user = WebCAS.getUser();
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

		JPAExtPaginator<models.order.Orders>  orderList = Orders.findMyOrders(user, createdAtBegin, createdAtEnd, status, goodsName,pageNumber, PAGE_SIZE);

		BreadcrumbList breadcrumbs = new BreadcrumbList("我的订单", "/orders");
		render(orderList, breadcrumbs);
	}

	/**
	 * 付款
	 */
	public static void pay(Long id) {
		redirect("http://www.uhuiladev.com/payment_info/" + id);
	}

	/**
	 * 订单详情
	 */
	public static void details(Long id) {
		//订单信息
		models.order.Orders orders = models.order.Orders.findById(id);
		List<OrderItems> orderItems = orders.orderItems;
		//收货信息
		BreadcrumbList breadcrumbs = new BreadcrumbList("我的订单", "/orders", "订单详情", "/orders/" + id);
		render(orders, orderItems, breadcrumbs);
	}


	public static void applyRefund(Long id, String applyNote){

		User user = WebCAS.getUser();
		models.order.Orders order = models.order.Orders.findById(id);
		if(order == null || !order.user.getId().equals(user.getId())){
			renderJSON("{\"error\":\"no such order\"}");
			return;
		}

		//查找原订单信息
		TradeBill tradeBill = null;
		List<OrderItems> orderItems = order.orderItems;

		if(order != null){
			tradeBill = TradeBill.find("byOrderIdAndTradeStatus", order.getId(), TradeStatus.SUCCESS).first();
		}
		if(order == null || tradeBill == null || orderItems == null){
			renderJSON("{\"error\":\"can not get the trade bill\"}");
			return;
		}

		for (OrderItems orderItem:orderItems) {
			//创建退款流程
			RefundBill refundBill = RefundUtil.create(tradeBill, order.getId(), orderItem.getId(),
					orderItem.salePrice, applyNote);
			RefundUtil.success(refundBill);
			//更改库存
			orderItem.goods.baseSale += 1;
			orderItem.goods.saleCount -= 1;
			orderItem.goods.save();


		}
		//更改订单状态
		order.status = OrderStatus.RETURNED;
		order.save();
		renderJSON("{\"error\":\"ok\"}");
	}

}
