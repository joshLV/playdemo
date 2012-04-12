package controllers;

import models.accounts.AccountType;
import models.consumer.User;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.OrdersCondition;

import org.apache.commons.lang.StringUtils;

import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.website.cas.SecureCAS;

@With(SecureCAS.class)
public class MyCoupons extends Controller {

	public static int PAGE_SIZE = 15;

	/**
	 * 我的券列表
	 */
	public static void coupons(CouponsCondition condition) {
		User user = SecureCAS.getUser();
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		if (condition == null) {
    		condition = new CouponsCondition();
    	}
		JPAExtPaginator<ECoupon> couponsList = ECoupon.userCouponsQuery(condition,user.getId(),AccountType.CONSUMER, pageNumber,  PAGE_SIZE);
		BreadcrumbList breadcrumbs = new BreadcrumbList("我的券订单", "/coupons");
		renderCond(condition);
		
		render("MyCoupons/e_coupons.html", couponsList, breadcrumbs);
	}

	/**
	 * 申请退款
	 * 
	 * @param id 券ID
	 * @param applyNote 退款原因
	 */
	public static void applyRefund(Long id, String applyNote){
		User user = SecureCAS.getUser();
		ECoupon eCoupon = ECoupon.findById(id);
		String returnFlg = ECoupon.applyRefund(eCoupon,user.getId(),applyNote);
		renderJSON(returnFlg);
	}
	
	/**
	 * 向页面设置选择信息
	 * 
	 * @param condition 页面设置选择信息
	 */
	private static void renderCond(CouponsCondition condition) {
		renderArgs.put("createdAtBegin", condition.createdAtBegin);
		renderArgs.put("createdAtEnd", condition.createdAtEnd);
		renderArgs.put("status", condition.status);
		renderArgs.put("goodsName", condition.goodsName);
	}
}
