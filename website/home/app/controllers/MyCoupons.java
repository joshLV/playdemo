package controllers;

import java.util.Date;

import models.accounts.AccountType;
import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
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
	public static void coupons(Date createdAtBegin, Date createdAtEnd, ECouponStatus status, String goodsName) {
		User user = SecureCAS.getUser();
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		JPAExtPaginator<ECoupon> couponsList = ECoupon.userCouponsQuery(user.getId(), AccountType.CONSUMER, createdAtBegin, createdAtEnd, status, goodsName,pageNumber, PAGE_SIZE);
		BreadcrumbList breadcrumbs = new BreadcrumbList("我的券订单", "/coupons");
		renderArgs.put("createdAtBegin", createdAtBegin);
		renderArgs.put("createdAtEnd", createdAtEnd);
		renderArgs.put("status", status);
		renderArgs.put("goodsName", goodsName);
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
}
