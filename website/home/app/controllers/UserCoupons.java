package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.accounts.AccountType;
import models.consumer.User;
import models.order.CouponsCondition;
import models.order.ECoupon;
import org.apache.commons.lang.StringUtils;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

@With({SecureCAS.class, WebsiteInjector.class})
public class UserCoupons extends Controller {

	public static int PAGE_SIZE = 15;

	/**
	 * 我的券列表
	 */
	public static void index(CouponsCondition condition) {
		User user = SecureCAS.getUser();
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		if (condition == null) {
    		condition = new CouponsCondition();
    	}
        condition.userId = user.id;
        condition.accountType = AccountType.CONSUMER;
		JPAExtPaginator<ECoupon> couponsList = ECoupon.query(condition, pageNumber, PAGE_SIZE);
		BreadcrumbList breadcrumbs = new BreadcrumbList("我的券", "/coupons");

		render(couponsList, breadcrumbs, user, condition);
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
		String returnFlg = ECoupon.applyRefund(eCoupon,user.getId(),applyNote, AccountType.CONSUMER);
		renderJSON(returnFlg);
	}
	

	/**
	 * 重发短信
	 * @param id
	 */
    public static void sendMessage(long id) {
    	boolean sendFalg = ECoupon.sendUserMessage(id);
        renderJSON(sendFalg ?"0":"1");
    }
}
