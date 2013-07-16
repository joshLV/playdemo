package jobs.account;

import com.uhuila.common.util.DateUtil;
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
 * User: wangjia
 * Date: 13-7-9
 * Time: 下午2:29
 */
//@On("0 0 4 * * ?")  //每天凌晨四点执行
//@OnApplicationStart
@JobDefine(title = "更新过去账户结算金额", description = "更新过去账户结算金额")
public class refreshPreviousClearAccount extends JobWithHistory {
    @Override
    public void doJobWithHistory() throws Exception {
        List<Account> accountList = Account.find("(accountType = ? or accountType = ?)",
                AccountType.SUPPLIER,
                AccountType.SHOP).fetch();
        System.out.println("accountList = " + accountList);
        ClearedAccount clearedAccount;
        Date toDate = DateUtil.stringToDate("2013-07-05 23:59:59", "yyyy-MM-dd HH:mm:ss");
        for (Account account : accountList) {
            List<AccountSequence> sequences = AccountSequence.find(
                    " account=?  and settlementStatus=? and createdAt <? and tradeType !=? ",
                    account, SettlementStatus.UNCLEARED, toDate, TradeType.WITHDRAW).fetch();

            clearedAccount = new ClearedAccount();
            clearedAccount.date = toDate;
            clearedAccount.accountId = account.id;
            clearedAccount.amount = AccountSequence.getClearAmount(account,
                    toDate);
            if (clearedAccount.amount.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            for (AccountSequence sequence : sequences) {
                sequence.settlementStatus = SettlementStatus.CLEARED;
                sequence.save();
            }
            clearedAccount.accountSequences = sequences;
            clearedAccount.save();
            Logger.info("account.id:" + account.id + "toDate:" + toDate + " " +
                    "getClearAmount:" + clearedAccount.amount);
        }

        for (int i = 0; i < 10; i++) {
            Date fromDate = DateUtils.truncate(DateUtils.addDays(new Date(), -1 - i), Calendar.DATE);
            toDate = DateUtils.truncate(DateUtils.addDays(new Date(), -i), Calendar.DATE);
            for (Account account : accountList) {
                List<AccountSequence> sequences = AccountSequence.find(
                        " account=?  and settlementStatus=? and createdAt >=? and createdAt <? and tradeType !=?",
                        account, SettlementStatus.UNCLEARED, fromDate, toDate, TradeType.WITHDRAW).fetch();

                clearedAccount = new ClearedAccount();
                clearedAccount.date = DateUtils.addSeconds(DateUtils.truncate(DateUtils.addDays(new Date(), -i),
                        Calendar.DATE), -1);
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
                Logger.info("account.id:" + account.id + " fromDate:" + fromDate + "toDate:" + toDate + " " +
                        "getClearAmount:" + clearedAccount.amount);
                clearedAccount.accountSequences = sequences;
                clearedAccount.save();
            }
        }

    }
}
