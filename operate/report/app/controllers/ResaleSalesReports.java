package controllers;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-18
 * Time: 下午4:36
 */

@With(OperateRbac.class)
public class ResaleSalesReports extends Controller {
    @ActiveNavigation("resale_sales_reports")
    public static void index() {
        render();
    }
}
