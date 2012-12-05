package controllers;

import models.ResaleSalesReport;
import models.ResaleSalesReportCondition;
import models.accounts.AccountType;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
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
public class ResaleSalesReports extends Controller {
    private static final int PAGE_SIZE = 30;

    @ActiveNavigation("resale_sales_reports")
    public static void index(ResaleSalesReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new ResaleSalesReportCondition();
        }
        List<ResaleSalesReport> resultList = null;
        // 查询出分销的所有结果
        if (condition.accountType == AccountType.RESALER) {
            resultList = ResaleSalesReport.query(condition);
        } else if (condition.accountType == AccountType.CONSUMER) {
            resultList = ResaleSalesReport.query(condition);
            //            resultList = ResaleSalesReport.queryConsumer(condition);

        } else {
            resultList = ResaleSalesReport.query(condition);
            List<ResaleSalesReport> consumerList = ResaleSalesReport.query(condition);
            //            List<ResaleSalesReport> consumerList = ResaleSalesReport.queryConsumer(condition);

            // 查询出所有结果
            for (ResaleSalesReport resaleSalesReport : consumerList) {
                resultList.add(resaleSalesReport);
            }
        }

        // 分页
        ValuePaginator<ResaleSalesReport> reportPage = PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);

        ResaleSalesReport summary = ResaleSalesReport.summary(resultList);
        render(reportPage, condition, summary);
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}

