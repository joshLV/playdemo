package controllers;

import java.util.List;
import models.SalesOrderItemReport;
import models.SalesOrderItemReportCondition;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import utils.PaginateUtil;

@With(OperateRbac.class)
public class SalesTaxReports extends Controller {

    private static final int PAGE_SIZE = 30;
    
    /**
     * 查询销售税务报表.
     *
     * @param condition
     */
    @ActiveNavigation("sales_tax_reports")
    public static void index(SalesOrderItemReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new SalesOrderItemReportCondition();
        }

        // 查询出所有结果
        List<SalesOrderItemReport> resultList =  SalesOrderItemReport.query(condition);
        // 分页
        ValuePaginator<SalesOrderItemReport> reportPage = PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);
        
        // 汇总
        SalesOrderItemReport summary = SalesOrderItemReport.summary(resultList);

        List<Supplier> supplierList = Supplier.findUnDeleted();
        render(reportPage, summary, condition, supplierList);
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}
