package factory.accounts;

import factory.ModelFactory;
import models.accounts.AccountType;
import models.accounts.WithdrawAccount;

/**
 * <p/>
 * User: yanjy
 * Date: 12-11-9
 * Time: 上午10:14
 */
public class WithdrawAccountFactory extends ModelFactory<WithdrawAccount> {
    @Override
    public WithdrawAccount define() {
        WithdrawAccount withdrawAccount = new WithdrawAccount();
        withdrawAccount.accountType = AccountType.SUPPLIER;
        withdrawAccount.bankCity = "上海";
        return withdrawAccount;
    }
}
