package controllers;

import com.uhuila.common.util.DateUtil;
import controllers.supplier.SupplierInjector;
import models.report.SupplierDailyReport;
import models.report.SupplierGoodsReport;
import models.report.SupplierShopReport;
import models.sales.Shop;
import models.supplier.Supplier;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 商户报表.
 * <p/>
 * User: sujie
 * Date: 1/7/13
 * Time: 5:18 PM
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class SupplierIncomeReports extends Controller {

    /**
     * 报表查询.
     *
     * @param shopId   门店标识
     * @param fromDate 开始日期
     * @param toDate   结束日期
     */
    @ActiveNavigation("reports")
    public static void index(String goodsShortName, Long shopId, Date fromDate, Date toDate) {
        final Supplier supplier = SupplierRbac.currentUser().supplier;
        Long supplierId = supplier.id;
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        List<models.sales.Goods> goodsList = models.sales.Goods.findDistinctShortNameBySupplierId(supplierId);
        if (fromDate == null) {
            fromDate = DateUtil.getBeforeDate(DateUtil.getBeginOfDay(), 8);
        }
        if (toDate == null) {
            toDate = DateUtil.getBeforeDate(DateUtil.getEndOfDay(), 2);
        } else if (StringUtils.isNotEmpty(supplier.shopEndHour)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(toDate);
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(supplier.shopEndHour.substring(0,2)));
            cal.set(Calendar.MINUTE, Integer.parseInt(supplier.shopEndHour.substring(3, 5)));
            toDate = cal.getTime();
        } else {
            toDate = DateUtil.getEndOfDay(toDate);
        }

        //商品报表
        List<SupplierGoodsReport> goodsReportList = SupplierGoodsReport.getConsumedCountList(supplier.id,
                goodsShortName, shopId, fromDate, toDate);
        //商品销量图
        if ((StringUtils.isBlank(goodsShortName)) && goodsList.size() > 1) {
            List<String> goodsNameList = new ArrayList<>();
            for (SupplierGoodsReport goodsReport : goodsReportList) {
                goodsNameList.add(goodsReport.goodsName);
            }
            Map<String, SupplierGoodsReport> goodsChartMap = SupplierGoodsReport.getChartMap(goodsReportList);
            renderArgs.put("goodsNameList", goodsNameList);
            renderArgs.put("goodsChartMap", goodsChartMap);
        }

        boolean showSellingState =false;
        if (supplier.showSellingState!= null && supplier.showSellingState) {
            goodsReportList = SupplierGoodsReport.getGoodsSellingReport(supplier.id, goodsShortName, shopId, fromDate, toDate);
            showSellingState=true;
        }
        //门店报表图
        if ((shopId == null || shopId == 0l) && shopList.size() > 1) {
            List<SupplierShopReport> shopChartList = SupplierShopReport.getChartList(supplier.id, goodsShortName, fromDate, toDate);
            List<String> shopNameList = new ArrayList<>();
            for (SupplierShopReport supplierShopReport : shopChartList) {
                shopNameList.add(supplierShopReport.shopName);
            }
            Map<String, SupplierShopReport> shopChartMap = SupplierShopReport.getChartMap(shopChartList);
            renderArgs.put("shopNameList", shopNameList);
            renderArgs.put("shopChartMap", shopChartMap);
        }
        //每日报表图
        List<SupplierDailyReport> dailyChartList = SupplierDailyReport.getChartList(supplier.id, goodsShortName, shopId, fromDate, toDate);
        Map<String, Long> dailyChartMap = SupplierDailyReport.getChartMap(dailyChartList);
        List<String> dailyList = DateUtil.getDateList(fromDate, toDate, 1, "yyyy-MM-dd");

        renderArgs.put("dailyList", dailyList);
        renderArgs.put("dailyChartMap", dailyChartMap);

        renderArgs.put("shopEndHour", StringUtils.isNotEmpty(supplier.shopEndHour) ? supplier.shopEndHour : "23:59");
        render(goodsShortName, shopId, fromDate, toDate, shopList, goodsList, goodsReportList,showSellingState);
    }

    /**
     * 设置开始结束日期的时间点.
     *
     * @param supplier 商户对象
     * @param fromDate 开始日期
     * @param toDate   结束日期
     */
    private static void setFromToDate(Supplier supplier, Date fromDate, Date toDate) {
        if (fromDate == null) {
            fromDate = DateUtil.getBeforeDate(DateUtil.getBeginOfDay(), 8);
        }
        if (toDate == null) {
            toDate = DateUtil.getBeforeDate(DateUtil.getEndOfDay(), 2);
        } else if (StringUtils.isNotEmpty(supplier.shopEndHour)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(toDate);
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(supplier.shopEndHour.substring(2)));
            cal.set(Calendar.MINUTE, Integer.parseInt(supplier.shopEndHour.substring(3, 5)));
            toDate = cal.getTime();
        } else {
            toDate = DateUtil.getEndOfDay(toDate);
        }
    }

    /**
     * 导出每日消费Excel报表.
     *
     * @param goodsShortName  商品短名称
     * @param shopId   门店标识
     * @param fromDate 开始日期
     * @param toDate   结束日期
     */
    public static void exportDailyReport(String goodsShortName, Long shopId, Date fromDate, Date toDate) {
        final Supplier supplier = SupplierRbac.currentUser().supplier;
        setFromToDate(supplier, fromDate, toDate);

        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "每日消费报表_" + DateUtil.dateToString(fromDate, 0) + "_" + DateUtil.dateToString(toDate, 0) + ".xls");

        List<SupplierDailyReport> dailyReportList = SupplierDailyReport.getChartList(supplier.id, goodsShortName, shopId, fromDate, toDate);

        render(dailyReportList, fromDate, toDate);
    }

    /**
     * 导出门店消费Excel报表.
     *
     * @param goodsShortName  商品短名称
     * @param shopId   门店标识
     * @param fromDate 开始日期
     * @param toDate   结束日期
     */
    public static void exportShopReport(String goodsShortName, Long shopId, Date fromDate, Date toDate) {
        final Supplier supplier = SupplierRbac.currentUser().supplier;
        setFromToDate(supplier, fromDate, toDate);

        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "门店消费报表_" + DateUtil.dateToString(fromDate, 0) + "_" + DateUtil.dateToString(toDate, 0) + ".xls");

        List<SupplierShopReport> shopReportList = SupplierShopReport.getChartList(supplier.id, goodsShortName, fromDate, toDate);

        render(shopReportList, fromDate, toDate);
    }


    /**
     * 导出商品消费Excel报表.
     *
     * @param goodsShortName  商品短名称
     * @param shopId   门店标识
     * @param fromDate 开始日期
     * @param toDate   结束日期
     */
    public static void exportGoodsReport(String goodsShortName, Long shopId, Date fromDate, Date toDate) {
        System.out.println( "here《=========:");
        final Supplier supplier = SupplierRbac.currentUser().supplier;
        setFromToDate(supplier, fromDate, toDate);

        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "商品消费报表_" + DateUtil.dateToString(fromDate, 0) + "_" + DateUtil.dateToString(toDate, 0) + ".xls");

        List<SupplierGoodsReport> goodsReportList = SupplierGoodsReport.getConsumedCountList(supplier.id,
                goodsShortName, shopId, fromDate, toDate);

        render(goodsReportList, fromDate, toDate);
    }
}
