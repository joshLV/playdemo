package controllers;

import models.order.CouponsCondition;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.report.DetailDailyReport;
import models.report.GoodsDailyReport;
import models.report.ReportCondition;
import models.report.ReportSummary;
import models.report.ShopDailyReport;
import models.report.TotalDailyReport;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 商户报表控制.
 * <p/>
 * User: sujie
 * Date: 5/16/12
 * Time: 4:07 PM
 */
@With(SupplierRbac.class)
public class SupplierReports extends Controller {
    private static final int PAGE_SIZE = 20;

    /**
     * 显示门店报表.
     */
    @ActiveNavigation("shops_report")
    public static void showShopReport(ReportCondition condition, int tabIndex) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new ReportCondition();
        }
        condition.supplier = SupplierRbac.currentUser().supplier;

        JPAExtPaginator reportPage = null;
        ReportSummary summary = null;
        switch (tabIndex) {
            case 0:
                reportPage = ShopDailyReport.query(condition, pageNumber, PAGE_SIZE);

                summary = ShopDailyReport.summary(condition);
                break;
            case 1:
                reportPage = DetailDailyReport.query(condition, pageNumber, PAGE_SIZE);

                summary = DetailDailyReport.summary(condition);
                break;
            case 2:
                CouponsCondition ecouponCondition = new CouponsCondition();
                ecouponCondition.supplier = condition.supplier;
                ecouponCondition.consumedAtBegin = condition.createdAtBegin;
                ecouponCondition.consumedAtEnd = condition.createdAtEnd;
                ecouponCondition.shopLike = condition.shopLike;
                ecouponCondition.goodsName = condition.goodsLike;
                ecouponCondition.status = ECouponStatus.CONSUMED;

                reportPage = ECoupon.query(ecouponCondition, pageNumber, PAGE_SIZE);
                summary = new ReportSummary();
                summary.goodsCount = reportPage.getRowCount();
                summary.originalAmount = ECoupon.sum(ecouponCondition);
                break;
        }
        render(reportPage, summary, condition, tabIndex);
    }

    /**
     * 显示商品报表.
     */
    @ActiveNavigation("goods_report")
    public static void showGoodsReport(ReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new ReportCondition();
        }
        condition.supplier = SupplierRbac.currentUser().supplier;


        JPAExtPaginator<GoodsDailyReport> reportPage = GoodsDailyReport.query(condition, pageNumber, PAGE_SIZE);

        ReportSummary summary = GoodsDailyReport.summary(condition);

        render(reportPage, summary, condition);
    }

    /**
     * 显示总报表.
     */
    @ActiveNavigation("total_report")
    public static void showTotalReport(ReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new ReportCondition();
        }
        condition.supplier = SupplierRbac.currentUser().supplier;

        JPAExtPaginator<TotalDailyReport> reportPage = TotalDailyReport.query(condition, pageNumber, PAGE_SIZE);

        ReportSummary summary = TotalDailyReport.summary(condition);

        render(reportPage, summary, condition);
    }


    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }

}