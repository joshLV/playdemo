package controllers;

import models.accounts.AccountSequenceCondition;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;

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
    }
}
