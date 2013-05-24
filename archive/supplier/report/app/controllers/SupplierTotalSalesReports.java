package controllers;

import models.order.ECoupon;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import models.totalsales.TotalSalesCondition;
import models.totalsales.TotalSalesReport;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import utils.PaginateUtil;

import java.util.List;
import java.util.Map;

@With(SupplierRbac.class)
public class SupplierTotalSalesReports extends Controller {

    private static final int PAGE_SIZE = 30;

    /**
     * 按日期汇总的销售趋势报表
     * 可按门店、商品、验证方式生成分组报表.
     */
    @ActiveNavigation("supplier_sales_trends_reports")
    public static void trends(TotalSalesCondition condition) {
        condition = initData(condition);
        int pageNumber = getPageNumber();
        if (condition.needQueryTrends()) {
          List<TotalSalesReport> totalSales = TotalSalesReport.queryTrends(condition);
            renderArgs.put("totalSales", totalSales);

            ValuePaginator<TotalSalesReport> reportPage = PaginateUtil.wrapValuePaginator(totalSales, pageNumber, PAGE_SIZE);
            TotalSalesReport summary = TotalSalesReport.summary(totalSales);

            List<String> dateList = TotalSalesReport.generateDateList(condition);
            Map<String, List<TotalSalesReport>> chartsMap = TotalSalesReport.mapTrendsCharts(totalSales, dateList);
            renderArgs.put("dateList", dateList);
            renderArgs.put("chartsMap", chartsMap);

            render(reportPage, summary, condition);
        }
        render();
    }

    /**
     * 按时间范围汇总的分布报表.
     * 可按门店、商品、验证方式生成分组报表.
     */
    @ActiveNavigation("supplier_sales_ratios_reports")
    public static void ratios(TotalSalesCondition condition) {
        condition = initData(condition);
        int pageNumber = getPageNumber();

        if (condition.needQueryRatios()) {
            List<TotalSalesReport> totalSales = TotalSalesReport.queryRatios(condition);
            renderArgs.put("totalSales", totalSales);

            ValuePaginator<TotalSalesReport> reportPage = PaginateUtil.wrapValuePaginator(totalSales, pageNumber, PAGE_SIZE);
            TotalSalesReport summary = TotalSalesReport.summary(totalSales);

            render(reportPage, summary, condition);
        }
        render();
    }

    /**
     * 明细列表
     */
    @ActiveNavigation("sales_report_app")
    public static void list(TotalSalesCondition condition) {
        condition = initData(condition);

        if (condition.needQueryTrends()) {
            List<ECoupon> ecoupons = TotalSalesReport.queryList(condition);
            renderArgs.put("ecoupons", ecoupons);
            TotalSalesReport summary = TotalSalesReport.summaryList(ecoupons);

            render(ecoupons, summary, condition);
        }
        render();
    }

    private static TotalSalesCondition initData(TotalSalesCondition condition) {
        if (condition == null) {
            condition = new TotalSalesCondition();
        }
        if (condition.type == 0) {
            condition.type = 1;
        }
        Supplier supplier = Supplier.findById(SupplierRbac.currentUser().supplier.id);
        condition.supplierId = supplier.id;
        condition.shopEndHour = supplier.shopEndHour;
        renderArgs.put("condition", condition);
        if (condition.supplierId != null && condition.supplierId > 0l) {
            List<Shop> shops = Shop.findShopBySupplier(condition.supplierId);
            renderArgs.put("shops", shops);

            List<Goods> allGoods = Goods.findBySupplierId(condition.supplierId);
            renderArgs.put("allGoods", allGoods);
        }
        return condition;
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }

}
