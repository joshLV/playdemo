package controllers;

import models.SalesReport;
import models.SalesReportCondition;
import models.accounts.AccountSequenceCondition;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;

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
    private static final int PAGE_SIZE = 10;

    @ActiveNavigation("sales_reports")
    public static void showSalesReport(SalesReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new SalesReportCondition();
        }
        System.out.println("interval>>>" + condition.interval);


//        List<SalesReport> resultList = SalesReport.query(condition);
        // 分页
//        ValuePaginator<SalesReport> reportPage = utils.PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);

        render(condition);

    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }

}
