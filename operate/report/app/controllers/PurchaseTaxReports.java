package controllers;

import java.util.ArrayList;
import java.util.List;

import com.uhuila.common.constants.DeletedStatus;
import models.PurchaseECouponReport;
import models.PurchaseECouponReportCondition;
import models.supplier.Supplier;
import operate.rbac.ContextedPermission;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import utils.PaginateUtil;

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
    public static void index(PurchaseECouponReportCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new PurchaseECouponReportCondition();
        }
        List<Supplier> supplierList;
        Boolean hasSeeAllSupplierPermission = ContextedPermission.hasPermission("SEE_ALL_SUPPLIER");
        if (hasSeeAllSupplierPermission) {
            supplierList = Supplier.findUnDeleted();
        } else {
            supplierList = Supplier.find(
                    "deleted=? and salesId=? order by createdAt DESC",
                    DeletedStatus.UN_DELETED,
                    OperateRbac.currentUser().id).fetch();
        }

        Long operatorId = OperateRbac.currentUser().id;
        List<PurchaseECouponReport> resultList = PurchaseECouponReport.query(condition, operatorId, hasSeeAllSupplierPermission);




        ValuePaginator<PurchaseECouponReport> reportPage = PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);

        PurchaseECouponReport summary = PurchaseECouponReport.summary(resultList);

//        List<Supplier> supplierList = Supplier.findUnDeleted();

        render(reportPage, summary, condition, supplierList);
    }


    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}