package controllers.real;

import controllers.OperateRbac;
import models.OperateResaleSalesReportCondition;
import models.RealGoodsSalesReport;
import models.RealGoodsSalesReportCondition;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Collections;
import java.util.List;

/**
 * User: yan
 * Date: 13-7-2
 * Time: 下午3:23
 */
@With(OperateRbac.class)
@ActiveNavigation("real_goods_order_reports")
public class RealGoodsSalesReports extends Controller {
    public static int PAGE_SIZE = 15;

    /**
     * 实物订单报表
     */
    @ActiveNavigation("real_goods_order_reports")
    public static void index(RealGoodsSalesReportCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        if (condition == null) {
            condition = new RealGoodsSalesReportCondition();
        }
        List<RealGoodsSalesReport> realGoodsOrderList = RealGoodsSalesReport.findByCondition(condition);
        // 分页
        ValuePaginator<RealGoodsSalesReport> reportPage = utils.PaginateUtil.wrapValuePaginator(realGoodsOrderList, pageNumber, PAGE_SIZE);
        RealGoodsSalesReport summary = RealGoodsSalesReport.getNetSummary(realGoodsOrderList);
        render(reportPage, summary, condition);
    }

    /**
     * 渠道实物销售报表
     */
    @ActiveNavigation("channel_real_sale_reports")
    public static void channelRealSaleReport(RealGoodsSalesReportCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        if (condition == null) {
            condition = new RealGoodsSalesReportCondition();
        }
        List<RealGoodsSalesReport> realGoodsOrderList = RealGoodsSalesReport.findChannleSales(condition);
        // 分页
        ValuePaginator<RealGoodsSalesReport> reportPage = utils.PaginateUtil.wrapValuePaginator(realGoodsOrderList, pageNumber, PAGE_SIZE);
        RealGoodsSalesReport summary = RealGoodsSalesReport.getChannelNetSummary(realGoodsOrderList);
        render(reportPage, summary, condition);
    }
}
