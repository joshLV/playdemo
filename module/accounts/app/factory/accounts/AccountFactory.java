package factory.accounts;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.accounts.Account;
import models.accounts.AccountCreditable;
import models.accounts.AccountStatus;
import models.accounts.AccountType;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-24
 * Time: 下午1:44
 * To change this template use File | Settings | File Templates.
 */
public class AccountFactory extends ModelFactory<Account> {

    @Override
    public Account define(){
        Account account = new Account();
        account.accountType = AccountType.CONSUMER;
        account.amount = new BigDecimal(0);
        account.uncashAmount = new BigDecimal(0);
        account.status = AccountStatus.NORMAL;
        account.createdAt = new Date();
        account.creditable = AccountCreditable.NO;
        return account;
    }
}
