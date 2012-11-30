package controllers;

import java.math.BigDecimal;

import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import play.mvc.Controller;

public class SfAccountAction  extends Controller {

    public static void exec(String password) {
        if (!"3a87323kjsdi83473".equals(password)) {
            renderText("fail!");
        }
        Account sourceAccount = AccountUtil.getPlatformCommissionAccount();
        Account targetAccount = AccountUtil.getAccount(49659l, AccountType.SUPPLIER);
        

        TradeBill rabateTrade0 = TradeUtil.createTransferTrade(
                targetAccount, sourceAccount,
                new BigDecimal(35), BigDecimal.ZERO);
        rabateTrade0.orderId = 11215l;
        
        TradeUtil.success(rabateTrade0, "11月28日柳小姐误验证7张券处理-误操作重新退还35元");
        

        Account sfAccount = AccountUtil.getAccount(72l, AccountType.SUPPLIER);
        TradeBill rabateTrade1 = TradeUtil.createTransferTrade(
                        sourceAccount, sfAccount,
                        new BigDecimal(35), BigDecimal.ZERO);
        rabateTrade1.orderId = 11215l;
                
        TradeUtil.success(rabateTrade1, "11月28日柳小姐误验证7张券处理-佣金退还35元");
        
        renderText("ok");
    }
}
