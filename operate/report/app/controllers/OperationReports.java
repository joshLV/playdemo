package controllers;

import models.*;
import models.accounts.AccountType;
import operate.rbac.ContextedPermission;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import utils.PaginateUtil;

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

    @ActiveNavigation("operation_reports_app")
    public static void index() {
        render();
    }

    @ActiveNavigation("sales_reports")
    public static void showSalesReport(SalesReportCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new SalesReportCondition();
        }
        Boolean hasSeeSalesRepotProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        List<SalesReport> resultList = SalesReport.query(condition);
        // 分页
        ValuePaginator<SalesReport> reportPage = utils.PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);

        // 汇总
        SalesReport summary = SalesReport.getNetSummary(resultList);

        render(condition, reportPage, hasSeeSalesRepotProfitRight, summary);

    }

    @ActiveNavigation("channel_reports")
    public static void showChannelReport(ResaleSalesReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new ResaleSalesReportCondition();
        }
        Boolean hasSeeSalesRepotProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");

        List<ResaleSalesReport> resultList = null;
        condition.accountType = null;
        resultList = ResaleSalesReport.query(condition);
        List<ResaleSalesReport> consumerList = ResaleSalesReport.queryConsumer(condition);

        // 查询出所有结果
        for (ResaleSalesReport resaleSalesReport : consumerList) {
            resultList.add(resaleSalesReport);
        }

        // 分页
        ValuePaginator<ResaleSalesReport> reportPage = PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);
        ResaleSalesReport summary = ResaleSalesReport.summary(resultList);
        render(reportPage, condition, summary, hasSeeSalesRepotProfitRight);
    }

    @ActiveNavigation("channel_category_reports")
    public static void showChannelCategoryReport(ChannelCategoryReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new ChannelCategoryReportCondition();
        }

        Boolean hasSeeSalesRepotProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");


        List<ResaleSalesReport> channelList = null;
        ResaleSalesReportCondition channelCondition = new ResaleSalesReportCondition();
        channelCondition.beginAt = condition.beginAt;
        channelCondition.endAt = condition.endAt;

        condition.accountType = null;
        channelList = ResaleSalesReport.query(channelCondition);
        List<ResaleSalesReport> channelConsumerList = ResaleSalesReport.queryConsumer(channelCondition);

        // 查询出所有结果
        for (ResaleSalesReport resaleSalesReport : channelConsumerList) {
            channelList.add(resaleSalesReport);
        }
        // 分页
        ValuePaginator<ResaleSalesReport> channelPage = PaginateUtil.wrapValuePaginator(channelList, pageNumber, PAGE_SIZE);

        ResaleSalesReport channelSummary = ResaleSalesReport.summary(channelList);


        List<ChannelCategoryReport> resultList = ChannelCategoryReport.query(condition);
        List<ChannelCategoryReport> consumerList = ChannelCategoryReport.queryConsumer(condition);

        // 查询出所有结果
        for (ChannelCategoryReport c : consumerList) {
            resultList.add(c);
        }


        // 分页
        ValuePaginator<ChannelCategoryReport> reportPage = utils.PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);


        render(condition, reportPage, channelPage, channelSummary);
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
            if (report.refundAmount == null) {
                report.refundAmount = BigDecimal.ZERO;
            }
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
            if (report.refundAmount == null) {
                report.refundAmount = BigDecimal.ZERO;
            }
        }
        render(salesReportList);
    }


    public static void channelReportWithPrivilegeExcelOut(ResaleSalesReportCondition condition) {
        if (condition == null) {
            condition = new ResaleSalesReportCondition();
        }
        String page = request.params.get("page");
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "渠道汇总报表_" + System.currentTimeMillis() + ".xls");

        List<ResaleSalesReport> resultList = null;
        condition.accountType = null;
        resultList = ResaleSalesReport.query(condition);
        List<ResaleSalesReport> consumerList = ResaleSalesReport.queryConsumer(condition);

        // 查询出所有结果
        for (ResaleSalesReport resaleSalesReport : consumerList) {
            resultList.add(resaleSalesReport);
        }


        for (ResaleSalesReport report : resultList) {
            if (report.refundPrice == null) {
                report.refundPrice = BigDecimal.ZERO;
            }
            if (report.realRefundPrice == null) {
                report.realRefundPrice = BigDecimal.ZERO;
            }
            if (report.consumedPrice == null) {
                report.consumedPrice = BigDecimal.ZERO;
            }
        }
        ResaleSalesReport summary = ResaleSalesReport.summary(resultList);
        System.out.println("summary>>>" + summary.buyNumber);
        render(resultList, summary);
    }

    public static void channelReportExcelOut(ResaleSalesReportCondition condition) {
        if (condition == null) {
            condition = new ResaleSalesReportCondition();
        }
        String page = request.params.get("page");
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "渠道汇总报表_" + System.currentTimeMillis() + ".xls");

        List<ResaleSalesReport> resultList = null;
        condition.accountType = null;
        resultList = ResaleSalesReport.query(condition);
        List<ResaleSalesReport> consumerList = ResaleSalesReport.queryConsumer(condition);

        // 查询出所有结果
        for (ResaleSalesReport resaleSalesReport : consumerList) {
            resultList.add(resaleSalesReport);
        }


        for (ResaleSalesReport report : resultList) {
            if (report.refundPrice == null) {
                report.refundPrice = BigDecimal.ZERO;
            }
            if (report.realRefundPrice == null) {
                report.realRefundPrice = BigDecimal.ZERO;
            }
            if (report.consumedPrice == null) {
                report.consumedPrice = BigDecimal.ZERO;
            }

        }
        render(resultList);
    }


    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }

}
