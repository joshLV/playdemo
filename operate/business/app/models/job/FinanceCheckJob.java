package models.job;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.util.AccountSequenceUtil;
import org.apache.commons.collections.CollectionUtils;
import play.jobs.Job;
import play.jobs.On;

import java.math.BigDecimal;
import java.util.List;

import static models.accounts.util.AccountSequenceUtil.MismatchBalance;
import static models.accounts.util.AccountSequenceUtil.checkAccountAmount;
import static models.accounts.util.AccountSequenceUtil.checkBalance;
import static models.accounts.util.AccountSequenceUtil.checkBalanceConservation;
import static models.accounts.util.AccountSequenceUtil.checkPromotionBalance;

/**
 * 财务核帐并修正的定时任务.
 * <p/>
 * User: sujie
 * Date: 1/14/13
 * Time: 11:02 AM
 */
@On("0 0 0 * * ?")  //每天凌晨执行,自动取消过期十天的未付款的订单
public class FinanceCheckJob extends Job {
    @Override
    public void doJob() throws Exception {
        System.out.println(")))))))))         Enter FinanceCheckJob.doJob");

        List<Account> accounts = Account.findAll();

        //检查帐号余额守恒状态
        long unconservationCount = checkBalanceConservation();
        System.out.println("unconservationCount:" + unconservationCount);

        //检查并修复财务流水及帐号余额
        checkAndFixBalance(accounts);

        //修复后再次检查帐号的余额
        checkAndFixAccountAmount(accounts);
        System.out.println(")))))))))         End of FinanceCheckJob.doJob");
    }

    private void checkAndFixBalance(List<Account> accounts) {
        for (Account account : accounts) {
            AccountSequence seq = checkAccountSequenceBalance(account);
            if (seq == null) {
                return;
            }
            System.out.println("Begin to fix ==> accountId:" + account.id + ",uid:" + account.uid
                    + ",accountType:" + account.accountType);
            AccountSequence lastAccountSequence = AccountSequence.getLastAccountSequence(account.id);

            int fixCount = AccountSequenceUtil.fixBalance(account);
            System.out.println("Fixed sequence count:" + fixCount);
            boolean isOk = AccountSequenceUtil.checkTradeBalance(account, lastAccountSequence);
            if (!isOk) {
                System.out.println("Fix cash balance failed.");
            } else {
                System.out.println("Fix cash balance success.");
                //如果帐号中记的现金余额和流水表中的不一致，就修改帐号表中的值
                if (account.amount.compareTo(lastAccountSequence.cashBalance) != 0) {
                    account.amount = lastAccountSequence.cashBalance;
                    account.save();
                    System.out.println("Fix Account Amount.");
                }
                if (account.uncashAmount.compareTo(lastAccountSequence.uncashBalance) != 0) {
                    account.uncashAmount = lastAccountSequence.uncashBalance;
                    account.save();
                    System.out.println("Fix Account UncashAmount.");
                }
            }
            boolean isPromotionOk = AccountSequenceUtil.checkTradePromotionBalance(account, lastAccountSequence);
            if (!isPromotionOk) {
                System.out.println("Fix promotion balance failed.");
            } else {
                System.out.println("Fix promotion balance success.");
                //如果帐号中记的活动金余额和流水表中的不一致，就修改帐号表中的值
                if (account.promotionAmount.compareTo(lastAccountSequence.promotionBalance) != 0) {
                    account.promotionAmount = lastAccountSequence.promotionBalance;
                    account.save();
                    System.out.println("Fix Account PromotionAmount.");
                }
            }
        }
    }

    private AccountSequence checkAccountSequenceBalance(Account account) {
        AccountSequence errBalanceSeq = checkBalance(account);
        if (errBalanceSeq != null) {
            System.out.println("error account balance(sequenceId:" + errBalanceSeq.id + ",accountId:" + account.id + ",uid:" + account.uid
                    + ",accountType:" + account.accountType + ",balance:" + errBalanceSeq.balance);
            return errBalanceSeq;
        }
        AccountSequence errPromotionSeq = checkPromotionBalance(account.id);
        if (errPromotionSeq != null) {
            System.out.println("error account promotion balance(sequenceId:" + errPromotionSeq.id + ",accountId:" + account.id + ",uid:" + account.uid
                    + ",promotionBalance:" + errPromotionSeq.promotionBalance);
            return errPromotionSeq;
        }
        return null;
    }

    private void checkAndFixAccountAmount(List<Account> accounts) {
        int i = 0;
        for (Account account : accounts) {

            List<AccountSequenceUtil.MismatchBalance> mismatchBalanceList = checkAccountAmount(account.id);
            if (CollectionUtils.isNotEmpty(mismatchBalanceList)) {
                System.out.print((++i) + "==>error account(id:" + account.id + ",uid:" + account.uid + ",type:" + account.accountType + ")");
                for (MismatchBalance mismatchBalance : mismatchBalanceList) {
                    System.out.print(": " + mismatchBalance);
                }
                System.out.println();
                //按财务流水修改帐号余额
                fixAccountAmount(account, mismatchBalanceList);
            }
        }
    }

    private void fixAccountAmount(Account account, List<MismatchBalance> mismatchBalanceList) {
        AccountSequence lastAccountSeq = AccountSequence.getLastAccountSequence(account.id);
        if (lastAccountSeq == null){
            System.out.println("lastAccountSequence is null. Do not fix:account(id:" + account.id + ",uid:" + account.uid + ",type:" + account.accountType + ")");
            return ;
        }
        for (MismatchBalance mismatchBalance : mismatchBalanceList) {

            switch (mismatchBalance) {
                case CASH_BALANCE:
                    account.amount = lastAccountSeq.cashBalance==null? BigDecimal.ZERO:lastAccountSeq.cashBalance;
                    break;
                case UNCASH_BALANCE:
                    account.uncashAmount = lastAccountSeq.uncashBalance==null? BigDecimal.ZERO:lastAccountSeq.uncashBalance;
                    break;
                case PROMOTION_BALANCE:
                    account.promotionAmount = lastAccountSeq.promotionBalance==null? BigDecimal.ZERO:lastAccountSeq.promotionBalance;
                    break;
            }
        }
        account.save();
    }
}
