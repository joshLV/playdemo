package controllers;

import java.util.List;
import models.PurchaseTaxReport;
import models.PurchaseTaxReportCondition;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 财务报表.
 * <p/>
 * User: sujie
 * Date: 5/3/12
 * Time: 4:30 PM
 */
@With(OperateRbac.class)
public class PurchaseTaxReports extends Controller {
    private static final int PAGE_SIZE = 30;

    /**
     * 查询采购税务报表.
     *
     * @param condition
     */
    @ActiveNavigation("purchase_tax_reports")
    public static void index(PurchaseTaxReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new PurchaseTaxReportCondition();
        }

        JPAExtPaginator<PurchaseTaxReport> reportPage = PurchaseTaxReport.query(condition, pageNumber, PAGE_SIZE);

        PurchaseTaxReport summary = PurchaseTaxReport.summary(condition);
        
        List<Supplier> supplierList = Supplier.findUnDeleted();

        render(reportPage, summary, condition, supplierList);
    }


    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}