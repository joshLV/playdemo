package controllers;

import models.order.OrderItems;
import models.order.OrdersCondition;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

@With(OperateRbac.class)
@ActiveNavigation("order_index")
public class OperateOrders extends Controller {

	public static int PAGE_SIZE = 15;

	/**
	 * 商户订单信息一览
	 *
	 * @param condition 页面条件信息
	 */
	public static void index(OrdersCondition condition) {
		if (condition == null) {
			condition = new OrdersCondition();
		}
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		JPAExtPaginator<models.order.Order> orderList = models.order.Order.query(condition, null, pageNumber, PAGE_SIZE);
		renderGoodsCond(condition);
		render(orderList);

	}

    /**
     * 订单发货.
     */
    public static void send(Long id) {
       index(null);
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
	 * 向页面设置选择信息
	 * 
	 * @param goodsCond 页面设置选择信息
	 */
	private static void renderGoodsCond(OrdersCondition goodsCond) {
		renderArgs.put("createdAtBegin", goodsCond.createdAtBegin);
		renderArgs.put("createdAtEnd", goodsCond.createdAtEnd);
		renderArgs.put("status", goodsCond.status);
		renderArgs.put("goodsName", goodsCond.goodsName);
		renderArgs.put("refundAtBegin", goodsCond.refundAtBegin);
		renderArgs.put("refundAtEnd", goodsCond.refundAtEnd);
		renderArgs.put("status", goodsCond.status);
		renderArgs.put("deliveryType", goodsCond.deliveryType);
		renderArgs.put("payMethod", goodsCond.payMethod);
		renderArgs.put("searchKey", goodsCond.searchKey);
		renderArgs.put("searchItems", goodsCond.searchItems);
	}

}
