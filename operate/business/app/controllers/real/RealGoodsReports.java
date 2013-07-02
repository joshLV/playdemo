package controllers.real;

import controllers.OperateRbac;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * User: yan
 * Date: 13-7-2
 * Time: 下午3:23
 */
@With(OperateRbac.class)
@ActiveNavigation("real_goods_order_reports")
public class RealGoodsReports extends Controller {
    /**
     * 实物订单报表
     */
    public static void showOrdersReport() {

        render();
    }

    /**
     * 实物退货报表
     */
    public static void showReTurnedReport() {
        render();
    }
}
