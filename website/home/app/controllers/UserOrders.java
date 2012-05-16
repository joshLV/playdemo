package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.accounts.AccountType;
import models.consumer.User;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrdersCondition;
import models.resale.Resaler;

import org.apache.commons.lang.StringUtils;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

@With({SecureCAS.class, WebsiteInjector.class})
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
		render(orderList, breadcrumbs,user,condition);
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
	     //加载用户账户信息
		User user = SecureCAS.getUser();

        //加载订单信息
        Order order = Order.find("byIdAndUserIdAndUserType", id, user.getId(), AccountType.CONSUMER).first();
        
		List<OrderItems> orderItems = order.orderItems;
		//收货信息
		BreadcrumbList breadcrumbs = new BreadcrumbList("我的订单", "/orders", "订单详情", "/orders/" + id);
		render(order, orderItems,breadcrumbs);
	}
	
	
	/**
	 * 订单详情
	 */
	public static void refund(Long id) {
	     //加载用户账户信息
		User user = SecureCAS.getUser();

        //加载订单信息
        Order order = Order.find("byIdAndUserIdAndUserType", id, user.getId(), AccountType.CONSUMER).first();
        
        List<ECoupon> eCoupons = ECoupon.findByOrder(order);
        System.out.println("eCoupons========"+eCoupons);
		//收货信息
		BreadcrumbList breadcrumbs = new BreadcrumbList("我的订单", "/orders", "申请退款", "/orders/refund/" + id);
		render(order, eCoupons,breadcrumbs);
	}

	/**
	 * 申请退款
	 * 
	 * @param couponIds ids
	 * @param orderId 订单ID
	 */
    public static void batchRefund(List<Long> couponIds , Long orderId){
    	User user = SecureCAS.getUser();
        if(couponIds == null || couponIds.size() == 0){
        	refund(orderId);
        }
        
        List<ECoupon> eCoupons = ECoupon.findByUserAndIds(couponIds, user.getId(), AccountType.CONSUMER);
        for(ECoupon eCoupon : eCoupons){
            ECoupon.applyRefund(eCoupon, user.getId(), "", AccountType.CONSUMER);
        }
        refund(orderId);
    }

}
