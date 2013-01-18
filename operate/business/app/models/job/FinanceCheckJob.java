package models.job;

import models.accounts.Account;
import models.accounts.util.AccountSequenceUtil;
import play.jobs.Job;
import play.jobs.On;

import java.util.Date;
import java.util.List;

/**
 * 财务核帐并修正的定时任务.
 * <p/>
 * User: sujie
 * Date: 1/14/13
 * Time: 11:02 AM
 */
@On("0 0 4 * * ?")  //每天凌晨执行
public class FinanceCheckJob extends Job {
    @Override
    public void doJob() throws Exception {
        System.out.println(")))))))))         Enter FinanceCheckJob.doJob");

        List<Account> accounts = Account.findAll();
        //检查帐号余额守恒状态
        System.out.println("=====Begin to check balance conservation");
        long count = AccountSequenceUtil.checkBalanceConservation();
        System.out.println("account_sequence的balance<>cashBalance+uncashBalance 的记录数:" + count);
        System.out.println("=====End to check balance conservation");

        Date from = null;
        //检查并修复财务流水
        System.out.println("=====Begin to check and fix sequence balance");
        AccountSequenceUtil.checkAndFixBalance(accounts, from);
        System.out.println("=====End to check and fix sequence balance");

        //修复后再次检查并修复帐号的余额
        System.out.println("=====Begin to check and fix account amount");
        AccountSequenceUtil.checkAndFixAccountAmount(accounts);
        System.out.println("=====End to check and fix account amount");
        System.out.println(")))))))))         End of FinanceCheckJob.doJob");
    }
}
