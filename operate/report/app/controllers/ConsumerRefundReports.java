package controllers;

import models.RefundReport;
import models.RefundReportCondition;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-20
 * Time: 上午9:57
 */
@With(OperateRbac.class)
public class ConsumerRefundReports extends Controller {
    private static final int PAGE_SIZE = 30;

    @ActiveNavigation("consumer_refund_reports")
    public static void index(RefundReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new RefundReportCondition();
        }
        List<RefundReport> resultList = RefundReport.getConsumerRefundData(condition);
        ValuePaginator<RefundReport> reportPage = utils.PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);
        RefundReport summary = RefundReport.consumerSummary(resultList);
        render(reportPage, condition,summary);
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}
