package controllers;

import models.SalesOrderItemReport;
import models.SalesOrderItemReportCondition;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

@With(OperateRbac.class)
public class SalesTaxReports extends Controller {

    private static final int PAGE_SIZE = 30;
    
    /**
     * 查询销售税务报表.
     *
     * @param condition
     */
    @ActiveNavigation("sales_tax_reports")
    public static void index(SalesOrderItemReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new SalesOrderItemReportCondition();
        }

        JPAExtPaginator<SalesOrderItemReport> reportPage = SalesOrderItemReport.query(condition, pageNumber, PAGE_SIZE);

        SalesOrderItemReport summary = SalesOrderItemReport.summary(condition);

        render(reportPage, summary, condition);
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}
