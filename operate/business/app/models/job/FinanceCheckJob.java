package models.job;

import com.uhuila.common.util.DateUtil;
import models.accounts.Account;
import models.accounts.util.AccountSequenceUtil;
import play.jobs.Every;
import play.jobs.Job;

import java.util.Date;
import java.util.List;

import static play.Logger.info;
import static play.Logger.warn;

/**
 * 财务核帐并修正的定时任务.
 * <p/>
 * User: sujie
 * Date: 1/14/13
 * Time: 11:02 AM
 */
//@On("0 0 4 * * ?")  //每天凌晨执行
@Every("1h")
public class FinanceCheckJob extends Job {

    @Override
    public void doJob() throws Exception {
        info(")))))))))         Enter FinanceCheckJob.doJob");

        List<Account> accounts = Account.findAll();
        //检查帐号余额守恒状态
        info("=====Begin to check balance conservation");
        long count = AccountSequenceUtil.checkBalanceConservation();
        if (count > 0) {
            warn("balance<>cashBalance+uncashBalance 的记录数:" + count);
        } else {
            info("balance<>cashBalance+uncashBalance 的记录数:" + count);
        }
        info("=====End to check balance conservation");

        //每次取2小时内的帐务流水进行检查并纠正
        Date from = DateUtil.getBeforeHour(new Date(), 2);
        //检查并修复财务流水
        info("=====Begin to check and fix sequence balance");
        AccountSequenceUtil.checkAndFixBalance(accounts, from);
        info("=====End to check and fix sequence balance");

        //修复后再次检查并修复帐号的余额
        info("=====Begin to check and fix account amount");
        AccountSequenceUtil.checkAndFixAccountAmount(accounts);
        info("=====End to check and fix account amount");
        info(")))))))))         End of FinanceCheckJob.doJob");
    }
}
