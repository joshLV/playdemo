package controllers;

import com.uhuila.common.util.DateUtil;
import models.PrepaymentDetailReport;
import models.PrepaymentReport;
import models.order.Prepayment;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.SimplePaginator;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import utils.PaginateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 预付款报表.
 * <p/>
 * User: sujie
 * Date: 11/27/12
 * Time: 2:26 PM
 */
@With(OperateRbac.class)
public class PrepaymentReports extends Controller {

    private static final int PAGE_SIZE = 30;
    private static final int INTERVAL_DAYS= 7;

    /**
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

    @ActiveNavigation("prepayment_reports")
    public static void trends(Long id) {
        Prepayment prepayment = Prepayment.findById(id);
        //x轴：日期间隔
        List<String> dateList = DateUtil.getDateList(prepayment.effectiveAt, prepayment.expireAt, INTERVAL_DAYS, "yyyy-M-dd");
        //y轴：
        Map<String, PrepaymentDetailReport> chartsMap = PrepaymentDetailReport.find(prepayment, dateList);

        List<PrepaymentDetailReport> reportList = new ArrayList<>();
        reportList.addAll(chartsMap.values());

        Collections.sort(reportList, new Comparator<PrepaymentDetailReport>() {
            @Override
            public int compare(PrepaymentDetailReport o1, PrepaymentDetailReport o2) {
                if (o1.date.after(o2.date)) {
                    return 1;
                } else if (o1.date.before(o2.date)) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        ValuePaginator<PrepaymentDetailReport> reportPage = PaginateUtil.wrapValuePaginator(reportList, getPageNumber(), PAGE_SIZE);
        render(prepayment, dateList, chartsMap, reportPage);
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}