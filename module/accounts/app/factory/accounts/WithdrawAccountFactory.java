package factory.accounts;

import models.accounts.AccountType;
import models.accounts.WithdrawAccount;
import models.supplier.Supplier;
import factory.FactoryBoy;
import factory.ModelFactory;

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
        withdrawAccount.userId = FactoryBoy.lastOrCreate(Supplier.class).id;
        withdrawAccount.bankName = "工商银行";
        withdrawAccount.subBankName = "徐家汇支行";
        withdrawAccount.bankCity = "上海";
        withdrawAccount.userName = "张三";
        withdrawAccount.cardNumber = "8888-88888-88888-8888";
        return withdrawAccount;
    }
}
