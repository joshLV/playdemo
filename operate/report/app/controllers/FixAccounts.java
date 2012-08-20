package controllers;

import java.math.BigDecimal;
import models.accounts.AccountSequence;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import play.mvc.Controller;

public class FixAccounts extends Controller {

    public static void fix() {
        fixOrder(4884L, BigDecimal.TEN);
        fixOrder(4898L, BigDecimal.TEN);
        fixOrder(5017L, BigDecimal.TEN);
    }

    private static void fixOrder(Long orderId, BigDecimal rebateValue) {

        AccountSequence as = AccountSequence.find(
                "orderId=? and remark like '活动折扣费%'", orderId).first();

        if (as == null) {
            TradeBill rabateTrade = TradeUtil.createTransferTrade(
                    AccountUtil.getUhuilaAccount(),
                    AccountUtil.getPlatformIncomingAccount(), rebateValue,
                    BigDecimal.ZERO);
            rabateTrade.orderId = orderId;
            TradeUtil.success(rabateTrade, "活动折扣费" + rebateValue);
        }
    }
}
