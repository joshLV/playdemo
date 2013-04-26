package controllers;

import models.accounts.*;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.resale.Resaler;
import operate.rbac.annotations.ActiveNavigation;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author likang
 */
@With(OperateRbac.class)
@ActiveNavigation("financing_incoming_note")
public class FinancingIncomingNote extends Controller{

    public static void add(BigDecimal amount, Long id){
        List<Account> accounts = Account.find("byAccountTypeAndCreditableAndStatus",
                AccountType.RESALER, AccountCreditable.YES, AccountStatus.NORMAL).fetch();
        for (Account account : accounts) {
            Resaler resaler = Resaler.findById(account.uid);
            if (resaler != null) {
                account.info = resaler.loginName;
            }
        }
        render(accounts, amount, id);
    }

    public static void create(BigDecimal amount, Long id, String comment) {
        Account account = null;
        if (id != null) {
            account = Account.findById(id);
        }
        if (account == null || !account.isCreditable()) {
            Validation.addError("financing_incoming.account", "未选择账户,或所选无效账户");
            Validation.keep();
            add(amount, id);
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            Validation.addError("financing_incoming.amount", "无效金额");
            Validation.keep();
            add(amount, id);
        }
        TradeBill bill = TradeUtil.transferTrade()
                .fromAccount(AccountUtil.getFinancingIncomingAccount())
                .toAccount(account)
                .balancePaymentAmount(amount)
                .make();
        TradeUtil.success(bill, "财务收到款项", comment, OperateRbac.currentUser().loginName);
        OperateReports.showFinancingIncomingReport(null);
    }
}
