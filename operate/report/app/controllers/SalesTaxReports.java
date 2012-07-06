package controllers;

import models.SalesTaxReport;
import models.SalesTaxReportCondition;
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
    public static void index(SalesTaxReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new SalesTaxReportCondition();
        }

        JPAExtPaginator<SalesTaxReport> reportPage = SalesTaxReport.query(condition, pageNumber, PAGE_SIZE);

        SalesTaxReport summary = SalesTaxReport.summary(condition);

        render(reportPage, summary, condition);
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}
