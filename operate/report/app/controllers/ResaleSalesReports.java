package controllers;

import models.ResaleSalesReport;
import models.ResaleSalesReportCondition;
import operate.rbac.annotations.ActiveNavigation;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import org.apache.commons.lang.StringUtils;
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

        // 查询出所有结果
        List<ResaleSalesReport> resultList = ResaleSalesReport.query(condition);
        // 分页
        ValuePaginator<ResaleSalesReport> reportPage = PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);


        render(reportPage, condition);
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}

