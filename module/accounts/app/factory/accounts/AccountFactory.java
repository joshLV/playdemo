package factory.accounts;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.accounts.Account;
import models.accounts.AccountType;
import models.consumer.User;

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
        User user = FactoryBoy.create(User.class);
        Account account = new Account(user.id, AccountType.CONSUMER);
        return account;
    }
}
