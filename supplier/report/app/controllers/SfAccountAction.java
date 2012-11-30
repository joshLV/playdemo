package controllers;

import java.math.BigDecimal;

import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import play.mvc.Controller;

public class SfAccountAction  extends Controller {

    public void exec(String password) {
        if (!"3a87323kjsdi83473".equals(password)) {
            renderText("fail!");
        }
        Account sourceAccount = AccountUtil.getPlatformCommissionAccount();
        Account targetAccount = AccountUtil.getAccount(49659l, AccountType.SUPPLIER);
        

        TradeBill rabateTrade = TradeUtil.createTransferTrade(
                sourceAccount, targetAccount,
                new BigDecimal(35), BigDecimal.ZERO);
        rabateTrade.orderId = 11215l;
        
        TradeUtil.success(rabateTrade, "11月28日柳小姐误验证7张券处理-佣金退还35元");
        
        renderText("ok");
    }
}
