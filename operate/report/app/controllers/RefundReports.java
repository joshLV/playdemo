package controllers;

import models.RefundReport;
import models.RefundReportCondition;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-7
 * Time: 下午4:04
 */
@With(OperateRbac.class)
@ActiveNavigation("refund_reports")
public class RefundReports extends Controller {
    private static final int PAGE_SIZE = 30;

    @ActiveNavigation("refund_reports")
    public static void index(RefundReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new RefundReportCondition();
        }
        List<RefundReport> resultList = RefundReport.query(condition);
        // 分页
        ValuePaginator<RefundReport> reportPage = utils.PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);
        List<Supplier> supplierList = Supplier.findUnDeleted();
        RefundReport summary = RefundReport.summary(resultList);
        render(reportPage, condition, supplierList, summary);
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}
