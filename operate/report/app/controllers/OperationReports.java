package controllers;

import models.*;
import models.admin.OperateUser;
import models.supplier.Supplier;
import operate.rbac.ContextedPermission;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import utils.PaginateUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 运营报表
 * <p/>
 * User: wangjia
 * Date: 12-12-11
 * Time: 下午3:05
 */
@With(OperateRbac.class)
public class OperationReports extends Controller {
    private static final int PAGE_SIZE = 50;

    @ActiveNavigation("operation_reports_app")
    public static void index() {
        render();
    }

    @ActiveNavigation("sales_reports")
    public static void showSalesReport(SalesReportCondition condition, String desc) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new SalesReportCondition();
        }
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        condition.hasSeeReportProfitRight = hasSeeReportProfitRight;
        condition.operatorId = OperateRbac.currentUser().id;

        // DESC 的值表示升降序，含11位，代表11个排序字段， 1 为升序， 2 为降序， 0 为不排序
        // 当无排序参数时，初始化 000002000000
        String orderBy = "";
        if (desc == null) {
            desc = "000002000000";
        }
        // 获取最新的desc值
        String[] descs = desc.split(",");
        desc = descs[descs.length - 1].trim();
        if (isValidDescInSalesReport(desc)) {
            int index = 0;
            // 定位排序属性
            for (int i = 0; i < desc.length(); i++) {
                if (desc.charAt(i) != '0') {
                    index = i;
                    orderBy = String.valueOf(i);
                    break;
                }
            }
            if (desc.charAt(index) == '1') {
                orderBy = orderBy + "1";
            } else {
                orderBy = orderBy + "2";
            }
        } else {
            orderBy = "52";
        }
        List<SalesReport> resultList = SalesReport.query(condition, orderBy);
        Collections.sort(resultList);
        // 分页
        ValuePaginator<SalesReport> reportPage = utils.PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);
        // 汇总
        SalesReport summary = SalesReport.getNetSummary(resultList);
        List<Supplier> supplierList = Supplier.findUnDeleted();

        render(condition, reportPage, hasSeeReportProfitRight, summary, desc, supplierList);
    }


    @ActiveNavigation("category_sales_reports")
    public static void showCategorySalesReport(CategorySalesReportCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new CategorySalesReportCondition();
        }
        condition.setDescFields();

        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        condition.hasSeeReportProfitRight = hasSeeReportProfitRight;
        condition.operatorId = OperateRbac.currentUser().id;
        List<CategorySalesReport> resultList = CategorySalesReport.query(condition);
        // 分页

        ValuePaginator<CategorySalesReport> reportPage = utils.PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);

        List<CategorySalesReport> totalList = CategorySalesReport.queryTotal(condition);
        // 汇总
        CategorySalesReport summary = CategorySalesReport.getNetSummary(totalList);
        List<Supplier> supplierList = Supplier.findUnDeleted();
        render(condition, reportPage, hasSeeReportProfitRight, summary, supplierList);
    }


    @ActiveNavigation("channel_reports")
    public static void showChannelReport(ResaleSalesReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new ResaleSalesReportCondition();
        }
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        condition.hasSeeReportProfitRight = hasSeeReportProfitRight;
        condition.operatorId = OperateRbac.currentUser().id;
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
        for (ResaleSalesReport r : resultList) {
            if (summary.amount.compareTo(BigDecimal.ZERO) != 0) {
                r.contribution = (r.salePrice == null ? BigDecimal.ZERO : r.salePrice).divide(summary.amount == null ? BigDecimal.ZERO : summary.amount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            } else {
                r.contribution = BigDecimal.ZERO;
            }
        }
        render(reportPage, condition, summary, hasSeeReportProfitRight);
    }

    @ActiveNavigation("consumer_flow_reports")
    public static void showConsumerFlowReport(ConsumerFlowReportCondition condition, String desc) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new ConsumerFlowReportCondition();
        }
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        condition.hasSeeReportProfitRight = hasSeeReportProfitRight;
        condition.operatorId = OperateRbac.currentUser().id;

        // DESC 的值表示升降序，含11位，代表11个排序字段， 1 为升序， 2 为降序， 0 为不排序
        // 当无排序参数时，初始化 020000000
        String orderBy = "";
        if (desc == null) {
            desc = "200000000";
        }
        // 获取最新的desc值
        String[] descs = desc.split(",");
        desc = descs[descs.length - 1].trim();
        if (isValidDescInConsumerFlowReport(desc)) {
            int index = 0;
            // 定位排序属性
            for (int i = 0; i < desc.length(); i++) {
                if (desc.charAt(i) != '0') {
                    index = i;
                    orderBy = String.valueOf(i);
                    break;
                }
            }
            if (desc.charAt(index) == '1') {
                orderBy = orderBy + "1";
            } else {
                orderBy = orderBy + "2";
            }
        } else {
            orderBy = "12";
        }
        List<ConsumerFlowReport> resultList = ConsumerFlowReport.query(condition, orderBy);
//        for (ConsumerFlowReport c : resultList) {
//            System.out.println(c.grossMargin + "===c.grossMargin>>");
//        }
        Collections.sort(resultList);

        // 分页
        ValuePaginator<ConsumerFlowReport> reportPage = utils.PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);


        // 汇总
        ConsumerFlowReport summary = ConsumerFlowReport.summary(resultList);
        render(condition, reportPage, hasSeeReportProfitRight, summary, desc);
    }


    @ActiveNavigation("people_effect_reports")
    public static void showPeopleEffectReport(SalesReportCondition condition) {
        Boolean flagWithCondition = true;
        int pageNumber = getPageNumber();
        if (condition == null) {

            condition = new SalesReportCondition();
        }
        if (StringUtils.isBlank(condition.jobNumber) || StringUtils.isBlank(condition.userName)) {
            flagWithCondition = false;
        }
        condition.salesId = OperateRbac.currentUser().id;
        condition.setDescFields();

        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        List<SalesReport> resultList = SalesReport.queryPeopleEffectData(condition);
//        if (flagWithCondition) {
        List<SalesReport> noContributionResultList = SalesReport.queryNoContributionPeopleEffectData(condition);

        Map<OperateUser, SalesReport> map = new HashMap<>();

        for (SalesReport resultItem : resultList) {
            map.put(getReportKeyOfPeopleEffect(resultItem), resultItem);
        }
        for (SalesReport noContributionItem : noContributionResultList) {
            SalesReport item = map.get(getReportKeyOfPeopleEffect(noContributionItem));
            if (item == null) {
                map.put(getReportKeyOfPeopleEffect(noContributionItem), noContributionItem);
            }
        }

        List fianlResultList = new ArrayList();
        for (OperateUser key : map.keySet()) {
            fianlResultList.add(map.get(key));
        }
        condition.sort(fianlResultList);


        // 分页
        ValuePaginator<SalesReport> reportPage = utils.PaginateUtil.wrapValuePaginator(fianlResultList, pageNumber, PAGE_SIZE);

        // 汇总
        SalesReport summary = SalesReport.getPeopleEffectSummary(fianlResultList);

        render(condition, reportPage, hasSeeReportProfitRight, summary);

    }

    @ActiveNavigation("people_effect_category_reports")
    public static void showPeopleEffectCategoryReport(PeopleEffectCategoryReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new PeopleEffectCategoryReportCondition();
        }

        condition.salesId = OperateRbac.currentUser().id;
        condition.setDescFields();
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");

        List<PeopleEffectCategoryReport> resultList = PeopleEffectCategoryReport.query(condition);
        // 分页
        ValuePaginator<PeopleEffectCategoryReport> reportPage = utils.PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);

        PeopleEffectCategoryReport summary = PeopleEffectCategoryReport.summary(reportPage);
        render(condition, reportPage, hasSeeReportProfitRight, summary);
    }

    /**
     * `
     * 查询净销售报表信息.
     *
     * @param condition
     */
    @ActiveNavigation("net_sales_reports")
    public static void showNetSalesReports(SalesOrderItemReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new SalesOrderItemReportCondition();
        }
        List<Supplier> supplierList;
        condition.hasSeeAllSupplierPermission = ContextedPermission.hasPermission("SEE_ALL_SUPPLIER");
        System.out.println(condition.hasSeeAllSupplierPermission + "===condition.hasSeeAllSupplierPermission>>");
        if (condition.hasSeeAllSupplierPermission) {
            supplierList = Supplier.findUnDeleted();
        } else {
            supplierList = Supplier.find(
                    "deleted=? and salesId=? order by createdAt DESC",
                    com.uhuila.common.constants.DeletedStatus.UN_DELETED,
                    OperateRbac.currentUser().id).fetch();
        }

        condition.operatorId = OperateRbac.currentUser().id;

        // 查询出所有结果
        List<SalesOrderItemReport> resultList = SalesOrderItemReport.getNetSales(condition);
        // 分页
        ValuePaginator<SalesOrderItemReport> reportPage = PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);

        // 汇总
        SalesOrderItemReport summary = SalesOrderItemReport.getNetSummary(resultList);

        render(reportPage, summary, condition, supplierList);


    }

    @ActiveNavigation("channel_category_reports")
    public static void showChannelCategoryReport(ChannelCategoryReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new ChannelCategoryReportCondition();
        }
        condition.setDescFields();
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        condition.hasSeeReportProfitRight = hasSeeReportProfitRight;
        condition.operatorId = OperateRbac.currentUser().id;
        List<ResaleSalesReport> channelPage = null;
        ResaleSalesReportCondition channelCondition = new ResaleSalesReportCondition();
        channelCondition.beginAt = condition.beginAt;
        channelCondition.endAt = condition.endAt;
        condition.accountType = null;
        channelPage = ResaleSalesReport.query(channelCondition);
        List<ResaleSalesReport> channelConsumerList = ResaleSalesReport.queryConsumer(channelCondition);
        // 查询出所有结果
        for (ResaleSalesReport resaleSalesReport : channelConsumerList) {
            channelPage.add(resaleSalesReport);
        }

        // 分页
//        ValuePaginator<ResaleSalesReport> channelPage = PaginateUtil.wrapValuePaginator(channelList, pageNumber, PAGE_SIZE);

        ResaleSalesReport channelSummary = ResaleSalesReport.summary(channelPage);


        List<ChannelCategoryReport> resultList = ChannelCategoryReport.query(condition);
        List<ChannelCategoryReport> consumerList = ChannelCategoryReport.queryConsumer(condition);

        // 查询出所有结果
        for (ChannelCategoryReport c : consumerList) {
            if (c.loginName == null) {
                c.code = "";
                c.name = "";
                c.loginName = "一百券";
            }
            resultList.add(c);
        }


        for (ChannelCategoryReport c : resultList) {
            if (channelSummary.amount.compareTo(BigDecimal.ZERO) != 0) {
                c.contribution = (c.salePrice == null ? BigDecimal.ZERO : c.salePrice).divide(channelSummary.amount == null ? BigDecimal.ZERO : channelSummary.amount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            } else {
                c.contribution = BigDecimal.ZERO;
            }
        }

        Collections.sort(resultList);
        // 分页
        ValuePaginator<ChannelCategoryReport> reportPage = utils.PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);


        render(condition, reportPage, channelPage, channelSummary, hasSeeReportProfitRight);
    }


    @ActiveNavigation("channel_goods_reports")
    public static void showChannelGoodsReport(ChannelGoodsReportCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new ChannelGoodsReportCondition();
        }
        condition.setDescFields();
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        condition.hasSeeReportProfitRight = hasSeeReportProfitRight;
        condition.operatorId = OperateRbac.currentUser().id;
        List<ChannelGoodsReport> resultList = ChannelGoodsReport.query(condition);
        List<ChannelGoodsReport> consumerList = ChannelGoodsReport.queryConsumer(condition);
        // 查询出所有结果
        for (ChannelGoodsReport c : consumerList) {
            if (c != null) {
                resultList.add(c);
            }
        }
        Collections.sort(resultList);
        //total
        List<ChannelGoodsReport> totalResultList = ChannelGoodsReport.queryTotal(condition);
        List<ChannelGoodsReport> totalConsumerResultList = ChannelGoodsReport.queryConsumerTotal(condition);

        // 查询出所有结果
        for (ChannelGoodsReport c : totalConsumerResultList) {
            totalResultList.add(c);
        }

        // 分页
        ValuePaginator<ChannelGoodsReport> reportPage = utils.PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);

        // 汇总
        ChannelGoodsReport summary = ChannelGoodsReport.getNetSummary(totalResultList);

        for (ChannelGoodsReport c : resultList) {
            if (summary.netSalesAmount.compareTo(BigDecimal.ZERO) != 0) {
                c.contribution = (c.netSalesAmount == null ? BigDecimal.ZERO : c.netSalesAmount).divide(summary.netSalesAmount == null ? BigDecimal.ZERO : summary.netSalesAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            } else {
                c.contribution = BigDecimal.ZERO;
            }
        }

        Collections.sort(resultList);

        List<Supplier> supplierList = Supplier.findUnDeleted();
        render(condition, reportPage, hasSeeReportProfitRight, summary, supplierList);

    }


    public static void salesReportWithPrivilegeExcelOut(SalesReportCondition condition, String desc) {
        if (condition == null) {
            condition = new SalesReportCondition();
        }
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        condition.hasSeeReportProfitRight = hasSeeReportProfitRight;
        condition.operatorId = OperateRbac.currentUser().id;

        String page = request.params.get("page");
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "销售报表_" + System.currentTimeMillis() + ".xls");
        // DESC 的值表示升降序，含11位，代表11个排序字段， 1 为升序， 2 为降序， 0 为不排序
        // 当无排序参数时，初始化 000002000000
        String orderBy = "";
        if (desc == null) {
            desc = "000002000000";
        }
        // 获取最新的desc值
        String[] descs = desc.split(",");
        desc = descs[descs.length - 1].trim();
        if (isValidDescInSalesReport(desc)) {
            int index = 0;
            // 定位排序属性
            for (int i = 0; i < desc.length(); i++) {
                if (desc.charAt(i) != '0') {
                    index = i;
                    orderBy = String.valueOf(i);
                    break;
                }
            }
            if (desc.charAt(index) == '1') {
                orderBy = orderBy + "1";
            } else {
                orderBy = orderBy + "2";
            }
        } else {
            orderBy = "52";
        }
        List<SalesReport> salesReportList = SalesReport.query(condition, orderBy);
        Collections.sort(salesReportList);


        for (SalesReport report : salesReportList) {
//            BigDecimal tempGrossMargin = report.grossMargin.divide(BigDecimal.valueOf(100));
//            report.grossMargin = tempGrossMargin;
            if (report.grossMargin == null) {
                report.grossMargin = BigDecimal.ZERO;
            }
            if (report.consumedAmount == null) {
                report.consumedAmount = BigDecimal.ZERO;
            }

            DecimalFormat df = new DecimalFormat("0.00");
            report.grossMargin = new BigDecimal(df.format(report.grossMargin));
            if (report.refundAmount == null) {
                report.refundAmount = BigDecimal.ZERO;
            }
            if (report.totalAmount == null) {
                report.totalAmount = BigDecimal.ZERO;
            }
            if (report.buyNumber == null) {
                report.buyNumber = 0l;
            }
            if (report.netSalesAmount == null) {
                report.netSalesAmount = BigDecimal.ZERO;
            }
            if (report.profit == null) {
                report.profit = BigDecimal.ZERO;
            }
            if (report.cheatedOrderAmount == null) {
                report.cheatedOrderAmount = BigDecimal.ZERO;
            }
        }
        render(salesReportList);
    }

    public static void salesReportExcelOut(SalesReportCondition condition, String desc) {
        if (condition == null) {
            condition = new SalesReportCondition();
        }
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        condition.hasSeeReportProfitRight = hasSeeReportProfitRight;
        condition.operatorId = OperateRbac.currentUser().id;
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "销售报表_" + System.currentTimeMillis() + ".xls");
        // DESC 的值表示升降序，含11位，代表11个排序字段， 1 为升序， 2 为降序， 0 为不排序
        // 当无排序参数时，初始化 000002000000
        String orderBy = "";
        if (desc == null) {
            desc = "000002000000";
        }
        // 获取最新的desc值
        String[] descs = desc.split(",");
        desc = descs[descs.length - 1].trim();
        if (isValidDescInSalesReport(desc)) {
            int index = 0;
            // 定位排序属性
            for (int i = 0; i < desc.length(); i++) {
                if (desc.charAt(i) != '0') {
                    index = i;
                    orderBy = String.valueOf(i);
                    break;
                }
            }
            if (desc.charAt(index) == '1') {
                orderBy = orderBy + "1";
            } else {
                orderBy = orderBy + "2";
            }
        } else {
            orderBy = "52";
        }
        List<SalesReport> salesReportList = SalesReport.query(condition, orderBy);
        Collections.sort(salesReportList);


        for (SalesReport report : salesReportList) {
            if (report.refundAmount == null) {
                report.refundAmount = BigDecimal.ZERO;
            }
            if (report.consumedAmount == null) {
                report.consumedAmount = BigDecimal.ZERO;
            }
            if (report.totalAmount == null) {
                report.totalAmount = BigDecimal.ZERO;
            }
            if (report.buyNumber == null) {
                report.buyNumber = 0l;
            }
            if (report.netSalesAmount == null) {
                report.netSalesAmount = BigDecimal.ZERO;
            }
            if (report.profit == null) {
                report.profit = BigDecimal.ZERO;
            }
            if (report.cheatedOrderAmount == null) {
                report.cheatedOrderAmount = BigDecimal.ZERO;
            }
        }
        render(salesReportList);
    }


    public static void channelReportWithPrivilegeExcelOut(ResaleSalesReportCondition condition) {
        if (condition == null) {
            condition = new ResaleSalesReportCondition();
        }
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        condition.hasSeeReportProfitRight = hasSeeReportProfitRight;
        condition.operatorId = OperateRbac.currentUser().id;
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

        ResaleSalesReport summary = ResaleSalesReport.summary(resultList);


        for (ResaleSalesReport report : resultList) {
//            BigDecimal tempGrossMargin = report.grossMargin.divide(BigDecimal.valueOf(100));
//            report.grossMargin = tempGrossMargin;
            if (summary.amount.compareTo(BigDecimal.ZERO) != 0) {
                report.contribution = (report.salePrice == null ? BigDecimal.ZERO : report.salePrice).divide(summary.amount == null ? BigDecimal.ZERO : summary.amount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2);
            } else {
                report.contribution = BigDecimal.ZERO;
            }

            if (report.grossMargin == null) {
                report.grossMargin = BigDecimal.ZERO;
            }


            DecimalFormat df = new DecimalFormat("0.00");
            report.grossMargin = new BigDecimal(df.format(report.grossMargin));
            if (report.refundPrice == null) {
                report.refundPrice = BigDecimal.ZERO;
            }
            if (report.realRefundPrice == null) {
                report.realRefundPrice = BigDecimal.ZERO;
            }
            if (report.consumedPrice == null) {
                report.consumedPrice = BigDecimal.ZERO;
            }


            if (report.channelCost == null) {
                report.channelCost = BigDecimal.ZERO;

            }

            if (report.profit == null) {
                report.profit = BigDecimal.ZERO;
            }
        }
        render(resultList);
    }

    public static void channelReportExcelOut(ResaleSalesReportCondition condition) {
        if (condition == null) {
            condition = new ResaleSalesReportCondition();
        }
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        condition.hasSeeReportProfitRight = hasSeeReportProfitRight;
        condition.operatorId = OperateRbac.currentUser().id;
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

        ResaleSalesReport summary = ResaleSalesReport.summary(resultList);

        for (ResaleSalesReport report : resultList) {
            if (summary.amount.compareTo(BigDecimal.ZERO) != 0) {
                report.contribution = (report.salePrice == null ? BigDecimal.ZERO : report.salePrice).divide(summary.amount == null ? BigDecimal.ZERO : summary.amount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2);
            } else {
                report.contribution = BigDecimal.ZERO;
            }
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


    public static void channelCategoryReportWithPrivilegeExcelOut(ChannelCategoryReportCondition condition) {
        if (condition == null) {
            condition = new ChannelCategoryReportCondition();
        }
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        condition.hasSeeReportProfitRight = hasSeeReportProfitRight;
        condition.operatorId = OperateRbac.currentUser().id;
        String page = request.params.get("page");
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "渠道大类报表_" + System.currentTimeMillis() + ".xls");

        List<ChannelCategoryReport> resultList = null;
        condition.accountType = null;
        resultList = ChannelCategoryReport.excelQuery(condition);
        List<ChannelCategoryReport> consumerList = ChannelCategoryReport.excelQueryConsumer(condition);

        // 查询出所有结果
        for (ChannelCategoryReport resaleSalesReport : consumerList) {
            resultList.add(resaleSalesReport);
        }

        ResaleSalesReportCondition channelCondition = new ResaleSalesReportCondition();
        channelCondition.beginAt = condition.beginAt;
        channelCondition.endAt = condition.endAt;
        condition.accountType = null;

        List<ResaleSalesReport> channelPage = null;
        channelPage = ResaleSalesReport.query(channelCondition);
        List<ResaleSalesReport> channelConsumerList = ResaleSalesReport.queryConsumer(channelCondition);
        // 查询出所有结果
        for (ResaleSalesReport resaleSalesReport : channelConsumerList) {
            channelPage.add(resaleSalesReport);
        }

        ResaleSalesReport channelSummary = ResaleSalesReport.summary(channelPage);


        for (ChannelCategoryReport report : resultList) {
//            BigDecimal tempGrossMargin = report.grossMargin.divide(BigDecimal.valueOf(100));
//            report.grossMargin = tempGrossMargin;
            if (channelSummary.amount.compareTo(BigDecimal.ZERO) != 0) {
                report.contribution = (report.salePrice == null ? BigDecimal.ZERO : report.salePrice).divide(channelSummary.amount == null ? BigDecimal.ZERO : channelSummary.amount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2);
            } else {
                report.contribution = BigDecimal.ZERO;
            }

            if (report.grossMargin == null) {
                report.grossMargin = BigDecimal.ZERO;
            }
            DecimalFormat df = new DecimalFormat("0.00");
            report.grossMargin = new BigDecimal(df.format(report.grossMargin));

            if (report.refundPrice == null) {
                report.refundPrice = BigDecimal.ZERO;
            }
            if (report.realRefundPrice == null) {
                report.realRefundPrice = BigDecimal.ZERO;
            }
            if (report.consumedPrice == null) {
                report.consumedPrice = BigDecimal.ZERO;
            }


            if (report.channelCost == null) {
                report.channelCost = BigDecimal.ZERO;

            }

            if (report.profit == null) {
                report.profit = BigDecimal.ZERO;
            }
        }
        render(resultList);
    }

    public static void channelCategoryReportExcelOut(ChannelCategoryReportCondition condition) {
        if (condition == null) {
            condition = new ChannelCategoryReportCondition();
        }
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        condition.hasSeeReportProfitRight = hasSeeReportProfitRight;
        condition.operatorId = OperateRbac.currentUser().id;
        String page = request.params.get("page");
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "渠道大类报表_" + System.currentTimeMillis() + ".xls");

        List<ChannelCategoryReport> resultList = null;
        condition.accountType = null;
        resultList = ChannelCategoryReport.excelQuery(condition);
        List<ChannelCategoryReport> consumerList = ChannelCategoryReport.excelQueryConsumer(condition);

        ResaleSalesReportCondition channelCondition = new ResaleSalesReportCondition();
        channelCondition.beginAt = condition.beginAt;
        channelCondition.endAt = condition.endAt;
        condition.accountType = null;

        List<ResaleSalesReport> channelPage = null;
        channelPage = ResaleSalesReport.query(channelCondition);
        List<ResaleSalesReport> channelConsumerList = ResaleSalesReport.queryConsumer(channelCondition);
        // 查询出所有结果
        for (ResaleSalesReport resaleSalesReport : channelConsumerList) {
            channelPage.add(resaleSalesReport);
        }

        ResaleSalesReport channelSummary = ResaleSalesReport.summary(channelPage);

        // 查询出所有结果
        for (ChannelCategoryReport resaleSalesReport : consumerList) {
            resultList.add(resaleSalesReport);
        }


        for (ChannelCategoryReport report : resultList) {
            if (channelSummary.amount.compareTo(BigDecimal.ZERO) != 0) {
                report.contribution = (report.salePrice == null ? BigDecimal.ZERO : report.salePrice).divide(channelSummary.amount == null ? BigDecimal.ZERO : channelSummary.amount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2);
            } else {
                report.contribution = BigDecimal.ZERO;
            }
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


    public static void channelGoodsReportWithPrivilegeExcelOut(ChannelGoodsReportCondition condition) {
        if (condition == null) {
            condition = new ChannelGoodsReportCondition();
        }
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        condition.hasSeeReportProfitRight = hasSeeReportProfitRight;
        condition.operatorId = OperateRbac.currentUser().id;
        String page = request.params.get("page");
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "渠道商品报表_" + System.currentTimeMillis() + ".xls");

        List<ChannelGoodsReport> resultList = null;
        resultList = ChannelGoodsReport.query(condition);
        List<ChannelGoodsReport> consumerList = ChannelGoodsReport.queryConsumer(condition);

        // 查询出所有结果
        for (ChannelGoodsReport resaleSalesReport : consumerList) {
            resultList.add(resaleSalesReport);
        }

        //total
        List<ChannelGoodsReport> totalResultList = ChannelGoodsReport.queryTotal(condition);
        List<ChannelGoodsReport> totalConsumerResultList = ChannelGoodsReport.queryConsumerTotal(condition);

        // 查询出所有结果
        for (ChannelGoodsReport c : totalConsumerResultList) {
            totalResultList.add(c);
        }

        // 汇总
        ChannelGoodsReport summary = ChannelGoodsReport.getNetSummary(totalResultList);

        for (ChannelGoodsReport report : resultList) {
//            BigDecimal tempGrossMargin = report.grossMargin.divide(BigDecimal.valueOf(100));
//            report.grossMargin = tempGrossMargin;
            if (summary.netSalesAmount.compareTo(BigDecimal.ZERO) != 0) {
                report.contribution = (report.netSalesAmount == null ? BigDecimal.ZERO : report.netSalesAmount).divide(summary.netSalesAmount == null ? BigDecimal.ZERO : summary.netSalesAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2);
            } else {
                report.contribution = BigDecimal.ZERO;
            }

            if (report.grossMargin == null) {
                report.grossMargin = BigDecimal.ZERO;
            }

            DecimalFormat df = new DecimalFormat("0.00");
            report.grossMargin = new BigDecimal(df.format(report.grossMargin));
            if (report.refundAmount == null) {
                report.refundAmount = BigDecimal.ZERO;
            }
            if (report.netSalesAmount == null) {
                report.netSalesAmount = BigDecimal.ZERO;
            }


            if (report.channelCost == null) {
                report.channelCost = BigDecimal.ZERO;

            }

            if (report.profit == null) {
                report.profit = BigDecimal.ZERO;
            }
            if (report.avgSalesPrice == null) {
                report.avgSalesPrice = BigDecimal.ZERO;
            }
            if (report.buyNumber == null) {
                report.buyNumber = 0l;
            }
            if (report.totalAmount == null) {
                report.totalAmount = BigDecimal.ZERO;
            }
            if (report.cheatedOrderAmount == null) {
                report.cheatedOrderAmount = BigDecimal.ZERO;
            }
            if (report.consumedAmount == null) {
                report.consumedAmount = BigDecimal.ZERO;
            }
            if (report.netCost == null) {
                report.netCost = BigDecimal.ZERO;
            }
        }
        render(resultList);
    }

    public static void channelGoodsReportExcelOut(ChannelGoodsReportCondition condition) {
        if (condition == null) {
            condition = new ChannelGoodsReportCondition();
        }
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        condition.hasSeeReportProfitRight = hasSeeReportProfitRight;
        condition.operatorId = OperateRbac.currentUser().id;
        String page = request.params.get("page");
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "渠道商品报表_" + System.currentTimeMillis() + ".xls");

        List<ChannelGoodsReport> resultList = null;
        resultList = ChannelGoodsReport.query(condition);
        List<ChannelGoodsReport> consumerList = ChannelGoodsReport.queryConsumer(condition);

        // 查询出所有结果
        for (ChannelGoodsReport resaleSalesReport : consumerList) {
            resultList.add(resaleSalesReport);
        }

        //total
        List<ChannelGoodsReport> totalResultList = ChannelGoodsReport.queryTotal(condition);
        List<ChannelGoodsReport> totalConsumerResultList = ChannelGoodsReport.queryConsumerTotal(condition);

        // 查询出所有结果
        for (ChannelGoodsReport c : totalConsumerResultList) {
            totalResultList.add(c);
        }

        // 汇总
        ChannelGoodsReport summary = ChannelGoodsReport.getNetSummary(totalResultList);

        for (ChannelGoodsReport report : resultList) {
            if (summary.netSalesAmount.compareTo(BigDecimal.ZERO) != 0) {
                report.contribution = (report.netSalesAmount == null ? BigDecimal.ZERO : report.netSalesAmount).divide(summary.netSalesAmount == null ? BigDecimal.ZERO : summary.netSalesAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2);
            } else {
                report.contribution = BigDecimal.ZERO;
            }

            if (report.refundAmount == null) {
                report.refundAmount = BigDecimal.ZERO;
            }
            if (report.netSalesAmount == null) {
                report.netSalesAmount = BigDecimal.ZERO;
            }
            if (report.avgSalesPrice == null) {
                report.avgSalesPrice = BigDecimal.ZERO;
            }
            if (report.buyNumber == null) {
                report.buyNumber = 0l;
            }
            if (report.totalAmount == null) {
                report.totalAmount = BigDecimal.ZERO;
            }
            if (report.cheatedOrderAmount == null) {
                report.cheatedOrderAmount = BigDecimal.ZERO;
            }
            if (report.consumedAmount == null) {
                report.consumedAmount = BigDecimal.ZERO;
            }
            if (report.netCost == null) {
                report.netCost = BigDecimal.ZERO;
            }

        }
        render(resultList);
    }

    /**
     * 人效报表导出
     *
     * @param condition
     */
    public static void peopleEffectReportExcelOut(SalesReportCondition condition, boolean hasRight) {
        if (condition == null) {
            condition = new SalesReportCondition();
        }
        condition.setDescFields();
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "人效报表_" + System.currentTimeMillis() + ".xls");
        List<SalesReport> peopleEffectReportList = SalesReport.queryPeopleEffectData(condition);
        for (SalesReport report : peopleEffectReportList) {
            if (hasRight) {
                BigDecimal tempGrossMargin = report.grossMargin == null ? BigDecimal.ZERO : report.grossMargin.divide(BigDecimal.valueOf(100));
                report.grossMargin = tempGrossMargin;
                report.profit = report.profit == null ? BigDecimal.ZERO : report.profit.setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        }

        if (hasRight) {
            render("OperationReports/peopleEffectReportWithPrivilegeExcelOut.xls", peopleEffectReportList);
        }

        render(peopleEffectReportList);
    }

    /**
     * 人效报表导出
     *
     * @param condition
     */
    public static void peopleEffectCategoryReportExcelOut(PeopleEffectCategoryReportCondition condition, boolean hasRight) {
        if (condition == null) {
            condition = new PeopleEffectCategoryReportCondition();
        }
        condition.setDescFields();
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "人效大类报表_" + System.currentTimeMillis() + ".xls");
        List<PeopleEffectCategoryReport> peopleEffectReportList = PeopleEffectCategoryReport.query(condition);
        for (PeopleEffectCategoryReport report : peopleEffectReportList) {
            if (hasRight) {
                BigDecimal tempGrossMargin = report.grossMargin == null ? BigDecimal.ZERO : report.grossMargin.divide(BigDecimal.valueOf(100));
                report.grossMargin = tempGrossMargin;
                report.profit = report.profit == null ? BigDecimal.ZERO : report.profit.setScale(2, BigDecimal.ROUND_HALF_UP);
                report.netProfit = report.netProfit == null ? BigDecimal.ZERO : report.netProfit.setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        }

        if (hasRight) {
            render("OperationReports/peopleEffectCategoryReportWithPrivilegeExcelOut.xls", peopleEffectReportList);
        }

        render(peopleEffectReportList);
    }

    public static void categorySalesReportWithPrivilegeExcelOut(CategorySalesReportCondition condition) {
        if (condition == null) {
            condition = new CategorySalesReportCondition();
        }
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        condition.hasSeeReportProfitRight = hasSeeReportProfitRight;
        condition.operatorId = OperateRbac.currentUser().id;
        String page = request.params.get("page");
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "大类销售报表_" + System.currentTimeMillis() + ".xls");

        List<CategorySalesReport> resultList = null;
        resultList = CategorySalesReport.excelQuery(condition);

        for (CategorySalesReport report : resultList) {
//            BigDecimal tempGrossMargin = report.grossMargin.divide(BigDecimal.valueOf(100));
//            report.grossMargin = tempGrossMargin;
            if (report.grossMargin == null) {
                report.grossMargin = BigDecimal.ZERO;
            }
            DecimalFormat df = new DecimalFormat("0.00");
            report.grossMargin = new BigDecimal(df.format(report.grossMargin));

            if (report.refundAmount == null) {
                report.refundAmount = BigDecimal.ZERO;
            }
            if (report.netSalesAmount == null) {
                report.netSalesAmount = BigDecimal.ZERO;
            }

            if (report.channelCost == null) {
                report.channelCost = BigDecimal.ZERO;

            }

            if (report.profit == null) {
                report.profit = BigDecimal.ZERO;
            }
            if (report.cheatedOrderAmount == null) {
                report.cheatedOrderAmount = BigDecimal.ZERO;
            }

            if (report.consumedAmount == null) {
                report.consumedAmount = BigDecimal.ZERO;
            }
            if (report.totalAmount == null) {
                report.totalAmount = BigDecimal.ZERO;
            }
            if (report.buyNumber == null) {
                report.buyNumber = 0l;
            }
            if (report.netSalesAmount == null) {
                report.netSalesAmount = BigDecimal.ZERO;
            }
            if (report.profit == null) {
                report.profit = BigDecimal.ZERO;
            }
        }
        render(resultList);
    }

    public static void categorySalesReportExcelOut(CategorySalesReportCondition condition) {
        if (condition == null) {
            condition = new CategorySalesReportCondition();
        }
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        condition.hasSeeReportProfitRight = hasSeeReportProfitRight;
        condition.operatorId = OperateRbac.currentUser().id;
        String page = request.params.get("page");
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "大类销售报表_" + System.currentTimeMillis() + ".xls");

        List<CategorySalesReport> resultList = null;
        resultList = CategorySalesReport.excelQuery(condition);

        for (CategorySalesReport report : resultList) {
            if (report.refundAmount == null) {
                report.refundAmount = BigDecimal.ZERO;
            }
            if (report.cheatedOrderAmount == null) {
                report.cheatedOrderAmount = BigDecimal.ZERO;
            }
            if (report.netSalesAmount == null) {
                report.netSalesAmount = BigDecimal.ZERO;
            }
            if (report.consumedAmount == null) {
                report.consumedAmount = BigDecimal.ZERO;
            }
            if (report.totalAmount == null) {
                report.totalAmount = BigDecimal.ZERO;
            }
            if (report.buyNumber == null) {
                report.buyNumber = 0l;
            }
            if (report.netSalesAmount == null) {
                report.netSalesAmount = BigDecimal.ZERO;
            }
            if (report.profit == null) {
                report.profit = BigDecimal.ZERO;
            }

        }
        render(resultList);
    }

    public static void consumerFlowReportWithPrivilegeExcelOut(ConsumerFlowReportCondition condition, String desc) {
        if (condition == null) {
            condition = new ConsumerFlowReportCondition();
        }
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        condition.hasSeeReportProfitRight = hasSeeReportProfitRight;
        condition.operatorId = OperateRbac.currentUser().id;
        String page = request.params.get("page");
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "客流报表_" + System.currentTimeMillis() + ".xls");

        List<ConsumerFlowReport> resultList = null;
        // DESC 的值表示升降序，含11位，代表11个排序字段， 1 为升序， 2 为降序， 0 为不排序
        // 当无排序参数时，初始化 020000000
        String orderBy = "";
        if (desc == null) {
            desc = "020000000";
        }
        // 获取最新的desc值
        String[] descs = desc.split(",");
        desc = descs[descs.length - 1].trim();
        if (isValidDescInConsumerFlowReport(desc)) {
            int index = 0;
            // 定位排序属性
            for (int i = 0; i < desc.length(); i++) {
                if (desc.charAt(i) != '0') {
                    index = i;
                    orderBy = String.valueOf(i);
                    break;
                }
            }
            if (desc.charAt(index) == '1') {
                orderBy = orderBy + "1";
            } else {
                orderBy = orderBy + "2";
            }
        } else {
            orderBy = "12";
        }
        resultList = ConsumerFlowReport.query(condition, orderBy);
        Collections.sort(resultList);

        for (ConsumerFlowReport report : resultList) {
//            BigDecimal tempGrossMargin = report.grossMargin.divide(BigDecimal.valueOf(100));
//            report.grossMargin = tempGrossMargin;
            if (report.grossMargin == null) {
                report.grossMargin = BigDecimal.ZERO;
            }

            DecimalFormat df = new DecimalFormat("0.00");
            report.grossMargin = new BigDecimal(df.format(report.grossMargin));
            if (report.refundPrice == null) {
                report.refundPrice = BigDecimal.ZERO;
            } else {
                report.refundPrice = new BigDecimal(df.format(report.refundPrice));
            }
            if (report.realRefundPrice == null) {
                report.realRefundPrice = BigDecimal.ZERO;
            } else {
                report.realRefundPrice = new BigDecimal(df.format(report.realRefundPrice));
            }
            if (report.consumedPrice == null) {
                report.consumedPrice = BigDecimal.ZERO;
            } else {
                report.consumedPrice = new BigDecimal(df.format(report.consumedPrice));
            }
            if (report.channelCost == null) {
                report.channelCost = BigDecimal.ZERO;
            } else {
                report.channelCost = new BigDecimal(df.format(report.channelCost));
            }

            if (report.profit == null) {
                report.profit = BigDecimal.ZERO;
            } else {
                report.profit = new BigDecimal(df.format(report.profit));
            }
            if (report.perOrderPrice == null) {
                report.perOrderPrice = BigDecimal.ZERO;
            } else {

                report.perOrderPrice = new BigDecimal(df.format(report.perOrderPrice));
            }
            if (report.salePrice == null) {
                report.salePrice = BigDecimal.ZERO;
            } else {
                report.salePrice = new BigDecimal(df.format(report.salePrice));
            }
            if (report.realSalePrice == null) {
                report.realSalePrice = BigDecimal.ZERO;
            } else {
                report.realSalePrice = new BigDecimal(df.format(report.realSalePrice));
            }
        }
        render(resultList);
    }

    public static void consumerFlowReportExcelOut(ConsumerFlowReportCondition condition, String desc) {
        if (condition == null) {
            condition = new ConsumerFlowReportCondition();
        }
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        condition.hasSeeReportProfitRight = hasSeeReportProfitRight;
        condition.operatorId = OperateRbac.currentUser().id;
        String page = request.params.get("page");
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "客流报表_" + System.currentTimeMillis() + ".xls");
        // DESC 的值表示升降序，含11位，代表11个排序字段， 1 为升序， 2 为降序， 0 为不排序
        // 当无排序参数时，初始化 020000000
        String orderBy = "";
        if (desc == null) {
            desc = "020000000";
        }
        // 获取最新的desc值
        String[] descs = desc.split(",");
        desc = descs[descs.length - 1].trim();
        if (isValidDescInConsumerFlowReport(desc)) {
            int index = 0;
            // 定位排序属性
            for (int i = 0; i < desc.length(); i++) {
                if (desc.charAt(i) != '0') {
                    index = i;
                    orderBy = String.valueOf(i);
                    break;
                }
            }
            if (desc.charAt(index) == '1') {
                orderBy = orderBy + "1";
            } else {
                orderBy = orderBy + "2";
            }
        } else {
            orderBy = "12";
        }

        List<ConsumerFlowReport> resultList = ConsumerFlowReport.query(condition, orderBy);
        Collections.sort(resultList);
        DecimalFormat df = new DecimalFormat("0.00");
        for (ConsumerFlowReport report : resultList) {
            if (report.refundPrice == null) {
                report.refundPrice = BigDecimal.ZERO;
            } else {
                report.refundPrice = new BigDecimal(df.format(report.refundPrice));
            }
            if (report.realRefundPrice == null) {
                report.realRefundPrice = BigDecimal.ZERO;
            } else {
                report.realRefundPrice = new BigDecimal(df.format(report.realRefundPrice));
            }
            if (report.consumedPrice == null) {
                report.consumedPrice = BigDecimal.ZERO;
            } else {
                report.consumedPrice = new BigDecimal(df.format(report.consumedPrice));
            }
            if (report.perOrderPrice == null) {
                report.perOrderPrice = BigDecimal.ZERO;
            } else {
                report.perOrderPrice = new BigDecimal(df.format(report.perOrderPrice));
            }
            if (report.salePrice == null) {
                report.salePrice = BigDecimal.ZERO;
            } else {
                report.salePrice = new BigDecimal(df.format(report.salePrice));
            }
            if (report.realSalePrice == null) {
                report.realSalePrice = BigDecimal.ZERO;
            } else {
                report.realSalePrice = new BigDecimal(df.format(report.realSalePrice));
            }
        }
        render(resultList);
    }


    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }

    /**
     * 判断排序字符串的合法性
     *
     * @param desc 排序字符串
     * @return
     */
    public static boolean isValidDescInSalesReport(String desc) {
        if (desc.length() != 12) {
            return false;
        }
        int countZero = 0;
        for (int i = 0; i < desc.length(); i++) {
            if (desc.charAt(i) == '0') {
                countZero++;
            }
        }
        if (countZero != 11) {
            return false;
        }
        for (int i = 0; i < desc.length(); i++) {
            if (desc.charAt(i) != '0' && desc.charAt(i) != '1' && desc.charAt(i) != '2') {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidDescInConsumerFlowReport(String desc) {
        if (desc.length() != 9) {
            return false;
        }
        int countZero = 0;
        for (int i = 0; i < desc.length(); i++) {
            if (desc.charAt(i) == '0') {
                countZero++;
            }
        }

        if (countZero != 8) {
            return false;
        }
        for (int i = 0; i < desc.length(); i++) {
            if (desc.charAt(i) != '0' && desc.charAt(i) != '1' && desc.charAt(i) != '2') {
                return false;
            }
        }
        return true;
    }

    private static OperateUser getReportKeyOfPeopleEffect(SalesReport item) {
        return item.operateUser;
    }

}
