package factory.accounts;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.accounts.Account;
import models.accounts.WithdrawBill;
import models.accounts.WithdrawBillStatus;

import java.math.BigDecimal;
import java.util.Date;


/**
 * 提现账单测试类.
 * User: wangjia
 * Date: 12-11-27
 * Time: 下午4:02
 */
public class WithdrawBillFactory extends ModelFactory<WithdrawBill> {
    @Override
    public WithdrawBill define() {
        WithdrawBill withdrawBill = new WithdrawBill();
        withdrawBill.serialNumber = "test" + FactoryBoy.sequence(WithdrawBill.class);
        withdrawBill.account = FactoryBoy.lastOrCreate(Account.class);
        withdrawBill.applier = "applier" + FactoryBoy.sequence(WithdrawBill.class);
        withdrawBill.amount = BigDecimal.TEN;
        withdrawBill.fee = BigDecimal.ZERO;
        withdrawBill.userName = "bankName" + FactoryBoy.sequence(WithdrawBill.class);
        withdrawBill.bankCity = "shanghai";
        withdrawBill.bankName = "bankName" + FactoryBoy.sequence(WithdrawBill.class);
        withdrawBill.subBankName = "subBankName" + FactoryBoy.sequence(WithdrawBill.class);
        withdrawBill.cardNumber = "cardNumber" + FactoryBoy.sequence(WithdrawBill.class);
        withdrawBill.status = WithdrawBillStatus.SUCCESS;
        withdrawBill.appliedAt = new Date();
        withdrawBill.accountName = "accountName" + FactoryBoy.sequence(WithdrawBill.class);
        return withdrawBill;
    }
}
