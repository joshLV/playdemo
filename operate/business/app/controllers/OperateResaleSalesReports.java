package controllers;

import models.OperateResaleSalesReport;
import models.OperateResaleSalesReportCondition;
import models.accounts.AccountType;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import utils.PaginateUtil;

import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-18
 * Time: 下午4:36
 */

@With(OperateRbac.class)
@ActiveNavigation("resale_sales_reports")
public class OperateResaleSalesReports extends Controller {
    private static final int PAGE_SIZE = 30;

//    @ActiveNavigation("resale_sales_reports")
    public static void index(OperateResaleSalesReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new OperateResaleSalesReportCondition();
        }
        List<OperateResaleSalesReport> resultList = null;
        // 查询出分销的所有结果
        if (condition.accountType == AccountType.RESALER) {
            resultList = OperateResaleSalesReport.query(condition);
        } else if (condition.accountType == AccountType.CONSUMER) {
            resultList = OperateResaleSalesReport.queryConsumer(condition);

        } else {
            resultList = OperateResaleSalesReport.query(condition);
            List<OperateResaleSalesReport> consumerList = OperateResaleSalesReport.queryConsumer(condition);

            // 查询出所有结果
            for (OperateResaleSalesReport resaleSalesReport : consumerList) {
                resultList.add(resaleSalesReport);
            }
        }


        Logger.info("Hello, condition=" + condition);

        // 分页
        ValuePaginator<OperateResaleSalesReport> reportPage = PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);
        OperateResaleSalesReport summary = OperateResaleSalesReport.summary(resultList);
        render(reportPage, condition, summary);
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}

