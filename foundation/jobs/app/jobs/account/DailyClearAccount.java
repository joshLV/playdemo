package jobs.account;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountType;
import models.accounts.ClearedAccount;
import models.accounts.SettlementStatus;
import models.accounts.TradeType;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 定时结算前一天相应账户结算金额
 * User: wangjia
 * Date: 13-7-5
 * Time: 下午3:22
 */
//@On("0 0 5 * * ?")  //每天凌晨四点执行
@JobDefine(title = "定时结算前一天相应账户结算金额", description = "定时结算前一天相应账户结算金额")
public class DailyClearAccount extends JobWithHistory {
    @Override
    public void doJobWithHistory() throws Exception {
        List<Account> accountList = Account.find("accountType = ? or accountType = ?", AccountType.SUPPLIER,
                AccountType.SHOP).fetch();
        ClearedAccount clearedAccount;
        Date fromDate = DateUtils.truncate(DateUtils.addDays(new Date(), -1), Calendar.DATE);
        Date toDate = DateUtils.truncate(new Date(), Calendar.DATE);
        for (Account account : accountList) {
            List<AccountSequence> sequences = AccountSequence.find(
                    " account=?  and settlementStatus=? and createdAt>=? and createdAt <? and tradeType !=?",
                    account, SettlementStatus.UNCLEARED, fromDate, toDate, TradeType.WITHDRAW).fetch();
            clearedAccount = new ClearedAccount();
            clearedAccount.date = DateUtils.addSeconds(DateUtils.truncate(new Date(), Calendar.DATE), -1);
            clearedAccount.accountId = account.id;
            clearedAccount.amount = AccountSequence.getClearAmount(account, fromDate,
                    clearedAccount.date);
            if (clearedAccount.amount.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            for (AccountSequence sequence : sequences) {
                sequence.settlementStatus = SettlementStatus.CLEARED;
                sequence.save();
            }
            clearedAccount.accountSequences = sequences;
            clearedAccount.save();
            Logger.info("account.id:" + account.id + " fromDate:" + fromDate + "toDate:" + toDate + " " +
                    "getClearAmount:" + clearedAccount.amount);
        }

    }
}
