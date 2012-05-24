package controllers;

import models.order.CouponsCondition;
import models.order.ECoupon;
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
    @ActiveNavigation("coupons_index")
    public static void index(CouponsCondition condition) {
        if (condition == null) {
            condition = new CouponsCondition();
        }

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        JPAExtPaginator<ECoupon> couponPage = ECoupon.query(condition, pageNumber, PAGE_SIZE);
        render(couponPage, condition);
    }

    /**
     * 冻结此券
     *
     * @param id
     */
    public static void freeze(long id) {
        ECoupon.freeze(id);
        index(null);
    }

    /**
     * 解冻此券
     *
     * @param id
     */
    public static void unfreeze(long id) {
        ECoupon.unfreeze(id);
        index(null);
    }


    /**
     * 重发短信
     *
     * @param id
     */
    public static void sendMessage(long id) {
        boolean sendFalg = ECoupon.sendMessage(id);
        renderJSON(sendFalg ? "0" : "1");
    }
}
