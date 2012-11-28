package controllers;

import models.PrepaymentReport;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.SimplePaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 11/27/12
 * Time: 2:26 PM
 */
@With(OperateRbac.class)
public class PrepaymentReports extends Controller {

    private static final int PAGE_SIZE = 30;

    /**
     * `
     * 查询预付款金额明细表.
     */
    @ActiveNavigation("prepayment_reports")
    public static void index(Long supplierId, int isEffective, String orderBy) {
        int pageNumber = getPageNumber();

        List<Supplier> supplierList = Supplier.findUnDeleted();
        // 查询出所有结果
        SimplePaginator<PrepaymentReport> reportPage = PrepaymentReport.getPage(supplierId, isEffective, orderBy, pageNumber, PAGE_SIZE);

        render(reportPage, supplierList, supplierId, isEffective, orderBy);
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}