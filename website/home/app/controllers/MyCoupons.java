package controllers;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import models.accounts.RefundBill;
import models.accounts.TradeBill;
import models.accounts.TradeStatus;
import models.accounts.util.RefundUtil;
import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.OrderItems;
import models.order.Order;
import controllers.modules.cas.SecureCAS;
import controllers.modules.webcas.WebCAS;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

@With({SecureCAS.class, WebCAS.class})
public class MyCoupons extends Controller {

	public static int PAGE_SIZE = 15;

	/**
	 * 我的券列表
	 */
	public static void coupons(Date createdAtBegin, Date createdAtEnd, ECouponStatus status, String goodsName) {
		User user = WebCAS.getUser();
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		JPAExtPaginator<ECoupon> couponsList = ECoupon.userCouponsQuery(user, createdAtBegin, createdAtEnd, status, goodsName,pageNumber, PAGE_SIZE);
		BreadcrumbList breadcrumbs = new BreadcrumbList("我的券订单", "/coupons");
		render("MyCoupons/e_coupons.html", couponsList, breadcrumbs);
	}

	/**
	 * 申请退款
	 * 
	 * @param id 券ID
	 * @param applyNote 退款原因
	 */
	public static void applyRefund(Long id, String applyNote){
		User user = WebCAS.getUser();
		ECoupon eCoupon = ECoupon.findById(id);
		String returnFlg = ECoupon.applyRefund(eCoupon,user.getId(),applyNote);
		renderJSON(returnFlg);
	}
}
