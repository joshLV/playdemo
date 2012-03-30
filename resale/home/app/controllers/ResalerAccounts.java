package controllers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import controllers.modules.cas.SecureCAS;
import controllers.resaletrace.ResaleCAS;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountSequenceCondition;
import models.accounts.AccountSequenceFlag;
import models.accounts.AccountType;

import models.accounts.util.AccountUtil;

import models.resale.Resaler;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 分销商账户明细控制器
 * 
 * @author likang
 *
 */
@With({SecureCAS.class, ResaleCAS.class})
public class ResalerAccounts extends Controller{
    private static final int PAGE_SIZE = 20;

    public static void index(AccountSequenceCondition condition){

        Resaler resaler = ResaleCAS.getResaler();
        Account account = AccountUtil.getAccount(resaler.getId(), AccountType.RESALER);

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        if (condition == null) {
            condition = new AccountSequenceCondition();
        }
        condition.account = account;
        JPAExtPaginator<AccountSequence> seqs = AccountSequence.findByAccount(condition,
                pageNumber, PAGE_SIZE);
        renderArgs.put("condition", condition);

        
        Map<AccountSequenceFlag, Object[]> summaryReport = AccountSequence.summaryReport(account);
        renderArgs.put("summaryReport", summaryReport);
        
        render(account, seqs);
    }

}
