package controllers;

import models.accounts.AccountSequenceCondition;
import models.accounts.AccountType;
import models.webop.BalanceReport;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;
import utils.CrossTableUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-8-22
 */
@With(OperateRbac.class)
public class BalanceReports extends Controller{

    @ActiveNavigation("balance_reports")
    public static void index(AccountSequenceCondition condition) {
        if (condition == null) {
            condition = new AccountSequenceCondition();
            condition.createdAtBegin = new Date();
            condition.createdAtEnd = new Date();
        }
        condition.accountTypes = new ArrayList<>();
        condition.accountTypes.add(AccountType.CONSUMER);
        condition.accountTypes.add(AccountType.RESALER);

        List<Map<String, Object>> reportPage = getReport(condition);
        render(reportPage, condition);
    }
    public static List<Map<String, Object>> getReport(AccountSequenceCondition condition) {
        List<BalanceReport> resultList = BalanceReport.queryWithdrawReport(condition);
        return  CrossTableUtil.generateCrossTable(resultList, BalanceReport.converter);
    }
}
