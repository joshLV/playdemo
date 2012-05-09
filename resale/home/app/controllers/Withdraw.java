package controllers;

import controllers.modules.resale.cas.SecureCAS;
import models.accounts.*;
import models.accounts.util.AccountUtil;
import models.resale.Resaler;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * @author  likang
 * Date: 12-5-7
 */
@With(SecureCAS.class)
public class Withdraw extends Controller{

    public static void index(){
        Resaler user = SecureCAS.getResaler();
        Account account = AccountUtil.getAccount(user.getId(), AccountType.RESALER);
        List<WithdrawBill> withdrawBills = WithdrawBill.find("account =? order by appliedAt desc",account).fetch();
        render(withdrawBills);
    }

    public static void apply(){
        Resaler user = SecureCAS.getResaler();
        Account account = AccountUtil.getAccount(user.getId(), AccountType.RESALER);
        render(account);
    }

    public static void create(@Valid WithdrawBill withdraw){
        Resaler user = SecureCAS.getResaler();
        Account account = AccountUtil.getAccount(user.getId(), AccountType.RESALER);

        if(Validation.hasErrors()){
            render("Withdraw/apply.html", withdraw, account);
        }
        if(withdraw.amount.compareTo(account.amount)>0){
            Validation.addError("withdraw.amount", "提现金额不能大于余额！！");
            render("Withdraw/apply.html", withdraw, account);
        }
        withdraw.apply(user.loginName, account);
        index();
    }
}
