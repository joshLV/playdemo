package controllers;

import models.accounts.AccountSequenceCondition;
import models.accounts.TradeType;
import models.webop.WithdrawReport;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import utils.CrossTableUtil;
import utils.PaginateUtil;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-8-2
 */
@With(OperateRbac.class)
public class WithdrawReports extends Controller {
    private static final int PAGE_SIZE = 20;

    /**
     * 查询分销商资金明细.
     *
     * @param condition 查询条件对象
     */
    @ActiveNavigation("withdraw_reports")
    public static void index(AccountSequenceCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new AccountSequenceCondition();
        }
        condition.tradeType = TradeType.WITHDRAW;
        List<Map<String, Object>> report = getReport(condition);
        // 分页
        ValuePaginator<Map<String, Object>> reportPage = PaginateUtil.wrapValuePaginator(report, pageNumber, PAGE_SIZE);
        render(reportPage, condition);
    }

    public static void download(AccountSequenceCondition condition){
        if (condition == null) {
            condition = new AccountSequenceCondition();
        }
        condition.tradeType = TradeType.WITHDRAW;
        List<Map<String, Object>> report = getReport(condition);
        response.contentType = "text/csv";
        response.setHeader("Content-Disposition", "attachment;filename=withdraw.csv");
        response.writeChunk("日期,消费者,商户,分销商");
        for (Map<String, Object> m : report) {
            response.writeChunk(m.get("RowKey") + ","
                    + (m.get("CONSUMER")) + ","
                    + (m.get("SUPPLIER")) + ","
                    + (m.get("RESALER")));
        }
    }

    public static List<Map<String, Object>> getReport(AccountSequenceCondition condition) {

        List<WithdrawReport> resultList = WithdrawReport.queryWithdrawReport(condition);

        return  CrossTableUtil.generateCrossTable(resultList, WithdrawReport.converter);
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}
