package factory.accounts;

import factory.ModelFactory;
import factory.annotation.Factory;
import models.accounts.Account;
import models.accounts.AccountCreditable;
import models.accounts.AccountStatus;
import models.accounts.AccountType;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 帐号的测试对象.
 *
 * User: hejun
 * Date: 12-8-24
 * Time: 下午1:44
 */
public class AccountFactory extends ModelFactory<Account> {

    @Override
    public Account define() {
        Account account = new Account();
        account.accountType = AccountType.CONSUMER;
        account.amount = BigDecimal.ZERO;
        account.uncashAmount = BigDecimal.ZERO;
        account.status = AccountStatus.NORMAL;
        account.createdAt = new Date();
        account.creditable = AccountCreditable.NO;
        return account;
    }

    @Factory(name = "balanceAccount")
    public void defineWithBalanceAccount(Account account) {
        account.accountType = AccountType.PLATFORM;
        account.uid=Account.PLATFORM_INCOMING;
        account.amount=BigDecimal.TEN;
    }

}
