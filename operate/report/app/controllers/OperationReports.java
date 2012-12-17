package controllers;

import models.SalesReport;
import models.SalesReportCondition;
import operate.rbac.ContextedPermission;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.List;

/**
 * 运营报表
 * <p/>
 * User: wangjia
 * Date: 12-12-11
 * Time: 下午3:05
 */
@With(OperateRbac.class)
public class OperationReports extends Controller {
    private static final int PAGE_SIZE = 15;

    @ActiveNavigation("sales_reports")
    public static void showSalesReport(SalesReportCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new SalesReportCondition();
        }
        Boolean hasSeeSalesRepotProfitRight = ContextedPermission.hasPermission("SEE_SALE_REPORT_PROFIT");
        List<SalesReport> resultList = SalesReport.query(condition);
        // 分页
        ValuePaginator<SalesReport> reportPage = utils.PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);

        // 汇总
        SalesReport summary = SalesReport.getNetSummary(resultList);

        render(condition, reportPage, hasSeeSalesRepotProfitRight, summary);

    }

    public static void salesReportWithPrivilegeExcelOut(SalesReportCondition condition) {
        if (condition == null) {
            condition = new SalesReportCondition();
        }
        String page = request.params.get("page");
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "销售报表_" + System.currentTimeMillis() + ".xls");

        List<SalesReport> salesReportList = SalesReport.query(condition);


        for (SalesReport report : salesReportList) {
            BigDecimal tempGrossMargin = report.grossMargin.divide(BigDecimal.valueOf(100));
            report.grossMargin = tempGrossMargin;
            if (report.refundAmount == null)
                report.refundAmount = BigDecimal.ZERO;
        }
        render(salesReportList);
    }

    public static void salesReportExcelOut(SalesReportCondition condition) {
        if (condition == null) {
            condition = new SalesReportCondition();
        }
        String page = request.params.get("page");
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "销售报表_" + System.currentTimeMillis() + ".xls");

        List<SalesReport> salesReportList = SalesReport.query(condition);


        for (SalesReport report : salesReportList) {
            if (report.refundAmount == null)
                report.refundAmount = BigDecimal.ZERO;
        }
        render(salesReportList);
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }

}
