package controllers;

import models.order.Order;
import models.sales.ConsultRecord;
import models.sales.ConsultResultCondition;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-24
 * Time: 下午12:37
 * To change this template use File | Settings | File Templates.
 */
@With(OperateRbac.class)
@ActiveNavigation("crm_app")
public class OperateConsult extends Controller {
    public static int PAGE_SIZE = 15;

    public static void index(ConsultResultCondition condition) {
        if (condition == null) {
            condition = new ConsultResultCondition();
        }

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
//        JPAExtPaginator<ConsultRecord> orderList = models.sales.ConsultRecord.query(condition, null, pageNumber, PAGE_SIZE);
//
//        BigDecimal amountSummary = Order.summary(orderList);
//        List<Brand> brandList = Brand.findByOrder(null);
//        renderArgs.put("brandList", brandList);
        render();

    }
}
