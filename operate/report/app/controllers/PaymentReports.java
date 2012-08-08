package controllers;

import models.webop.PaymentReport;
import models.accounts.*;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import utils.CrossTableUtil;
import utils.PaginateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-8-2
 */
@With(OperateRbac.class)
public class PaymentReports extends Controller {
    private static final int PAGE_SIZE = 20;

    /**
     * 查询分销商资金明细.
     *
     * @param condition 查询条件对象
     */
    @ActiveNavigation("payment_reports")
    public static void index(AccountSequenceCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new AccountSequenceCondition();
        }
        List<Account> accounts = new ArrayList<>();
        accounts.add(PaymentReport.alipayAccount);
        accounts.add(PaymentReport.tenpayAccount);
        accounts.add(PaymentReport.kuaiqianAccount);
        condition.accounts = accounts;

        List<PaymentReport> resultList = PaymentReport.queryPaymentReport(condition);

        List<Map<String, Object>>  report = CrossTableUtil.generateCrossTable(resultList, PaymentReport.converter);
        // 分页
        ValuePaginator<Map<String, Object>> reportPage = PaginateUtil.wrapValuePaginator(report, pageNumber, PAGE_SIZE);
        render(reportPage, condition);
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}
