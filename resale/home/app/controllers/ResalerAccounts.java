package controllers;

import java.util.Map;
import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountSequenceCondition;
import models.accounts.AccountSequenceFlag;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.resale.Resaler;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.resale.cas.SecureCAS;

/**
 * 分销商账户明细控制器
 * 
 * @author likang
 *
 */
@With(SecureCAS.class)
public class ResalerAccounts extends Controller{
    private static final int PAGE_SIZE = 20;

    public static void index(AccountSequenceCondition condition){

        Resaler resaler = SecureCAS.getResaler();
        Account account = AccountUtil.getAccount(resaler.getId(), AccountType.RESALER);

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
