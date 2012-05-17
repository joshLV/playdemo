package controllers;

import models.order.ECoupon;
import models.resale.Resaler;
import operate.rbac.annotations.ActiveNavigation;

import org.apache.commons.lang.StringUtils;

import play.modules.paginate.ModelPaginator;
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
		ModelPaginator<models.order.ECoupon> couponsList = ECoupon.queryCoupons(null, pageNumber, PAGE_SIZE);
		render(couponsList);
	}


	/**
	 *  冻结此券
	 * @param id
	 */
    public static void freeze(long id) {
    	ECoupon.freeze(id);
        index();
    }

	/**
	 *  解冻此券
	 * @param id
	 */
    public static void unfreeze(long id) {
    	ECoupon.unfreeze(id);
        index();
    }
    

	/**
	 * 重发短信
	 * @param id
	 */
    public static void sendMessage(long id) {
    	boolean sendFalg = ECoupon.sendMessage(id);
        renderJSON(sendFalg ?"0":"1");
    }
}
