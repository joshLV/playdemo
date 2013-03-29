package controllers.supplier;

import controllers.OperateRbac;
import models.accounts.Account;
import models.accounts.util.AccountSequenceUtil;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * User: tanglq
 * Date: 13-3-29
 * Time: 下午5:33
 */
@With(OperateRbac.class)
public class AccountFix extends Controller {
    /**
     * 手工调用account fix的方法
     * @param accountId
     */
    public static void fix(Long accountId) {
        Account account = Account.findById(accountId);

        if (account == null) {
            renderText("找不到对应的Account.");
        }
        Logger.info("accountId=" + account.id);
        BigDecimal oldAmount = account.amount;
        List<Account> accounts = new ArrayList<>();
        accounts.add(account);
        AccountSequenceUtil.checkAndFixBalance(accounts, null);  //修改
        account.refresh();
        renderText("修复成功！ old Amount=" + oldAmount + ", new Amount=" + account.amount);
    }
}
