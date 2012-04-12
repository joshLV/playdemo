package controllers;

import java.util.Date;

import models.accounts.AccountType;
import models.consumer.User;
import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.resale.Resaler;

import org.apache.commons.lang.StringUtils;

import controllers.modules.resale.cas.SecureCAS;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 券订单列表.
 * 
 * @author likang
 *
 */
@With(SecureCAS.class)
public class Coupons extends Controller{
	private static final int PAGE_SIZE = 20;

	public static void index(CouponsCondition condition){
		Resaler user = SecureCAS.getResaler();
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		if (condition == null) {
			condition = new CouponsCondition();
		}
		JPAExtPaginator<ECoupon> couponsList = ECoupon.userCouponsQuery(condition,user.getId(), AccountType.RESALER,pageNumber,  PAGE_SIZE);
		BreadcrumbList breadcrumbs = new BreadcrumbList("我的券订单", "/coupons");

		renderCond(condition);

		render(couponsList, breadcrumbs);

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
		renderArgs.put("orderNumber", condition.orderNumber);
		renderArgs.put("phone", condition.phone);
	}
}
