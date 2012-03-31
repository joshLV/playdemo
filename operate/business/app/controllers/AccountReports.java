package controllers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountSequenceCondition;
import models.accounts.AccountSequenceFlag;
import models.accounts.util.AccountUtil;
import controllers.operate.cas.SecureCAS;
import operate.rbac.annotations.ActiveNavigation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 系统账户报表查看
 * 
 * @author likang
 *
 */
@With({SecureCAS.class, OperateRbac.class})
public class AccountReports extends Controller{
    private static final int PAGE_SIZE = 20;

    @ActiveNavigation("account_reports")
    public static void index(AccountSequenceCondition condition){
        Account account = AccountUtil.getUhuilaAccount();
        
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        String interval = request.params.get("interval");
        renderArgs.put("interval", interval);

        if (condition == null) {
            condition = new AccountSequenceCondition();
        }else{
            renderArgs.put("createdAtBegin", condition.createdAtBegin);
            renderArgs.put("createdAtEnd", condition.createdAtEnd);
        }
        condition.account = account;
        JPAExtPaginator<AccountSequence> seqs = AccountSequence.findByAccount(condition,
                pageNumber, PAGE_SIZE);

        
        Map<AccountSequenceFlag, Object[]> summaryReport = AccountSequence.summaryReport(account);
        renderArgs.put("summaryReport", summaryReport);
        
        render(account, seqs);
    }
}
