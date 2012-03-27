package controllers;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountSequenceCondition;
import navigation.annotations.ActiveNavigation;
import navigation.annotations.Right;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;
import controllers.supplier.cas.SecureCAS;


/**
 * 商户的现金账户控制器.
 * <p/>
 * User: sujie
 * Date: 3/7/12
 * Time: 3:35 PM
 */
@With({SecureCAS.class, MenuInjector.class})
public class AccountSequences extends Controller {

    private static final int PAGE_SIZE = 20;

    @Right("STATS")
    @ActiveNavigation("account_sequence")
    public static void index(AccountSequenceCondition condition) {
        long accountId = 1; //todo 管理员登录做完后修改此处

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        Account account = Account.findById(accountId);
        if (condition == null) {
            condition = new AccountSequenceCondition();
        }
        condition.account = account;
        JPAExtPaginator<AccountSequence> accountSequences = AccountSequence.findByAccount(condition,
                pageNumber, PAGE_SIZE);
        renderArgs.put("condition", condition);
        render(account, accountSequences);
    }

}