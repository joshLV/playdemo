package controllers;

import java.util.List;

import models.order.ECoupon;
import models.order.OrderItems;
import models.order.OrdersCondition;
import operate.rbac.annotations.ActiveNavigation;

import org.apache.commons.lang.StringUtils;

import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

@With(OperateRbac.class)
@ActiveNavigation("coupons_index")
public class OperateCoupons extends Controller {

	public static int PAGE_SIZE = 15;

	/**
	 * 券号列表
	 */
	public static void index() {
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		JPAExtPaginator<models.order.ECoupon> couponsList = ECoupon.queryCoupons(null, pageNumber, PAGE_SIZE);
		render(couponsList);
	}

}
