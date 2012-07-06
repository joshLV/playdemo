package controllers;

import models.report.GoodsDailyReport;
import models.report.PurchaseTaxReport;
import models.report.ReportCondition;
import models.report.ReportSummary;
import models.report.SalesTaxReport;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 财务报表.
 * <p/>
 * User: sujie
 * Date: 5/3/12
 * Time: 4:30 PM
 */
@With(OperateRbac.class)
public class Reports extends Controller {
    private static final int PAGE_SIZE = 10;

    /**
     * 查询采购税务报表.
     *
     * @param condition
     */
    @ActiveNavigation("purchase_tax_reports")
    public static void showPurchaseTaxReport(ReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new ReportCondition();
        }

        JPAExtPaginator<GoodsDailyReport> reportPage = GoodsDailyReport.query(condition, pageNumber, PAGE_SIZE);

        ReportSummary summary = PurchaseTaxReport.summary(condition);

        render(reportPage, summary, condition);
    }

    /**
     * 查询销售税务报表.
     *
     * @param condition
     */
    @ActiveNavigation("sales_tax_reports")
    public static void showSalesTaxReport(ReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new ReportCondition();
        }

        JPAExtPaginator<SalesTaxReport> reportPage = SalesTaxReport.query(condition, pageNumber, PAGE_SIZE);

        ReportSummary summary = SalesTaxReport.summary(condition);

        render(reportPage, summary, condition);
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}