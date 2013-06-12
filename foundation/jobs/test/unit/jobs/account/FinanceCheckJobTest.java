package unit.jobs.account;

import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.AccountCreditable;
import models.accounts.AccountSequence;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import jobs.account.FinanceCheckJob;
import models.jobs.JobWithHistory;
import models.operator.Operator;
import models.resale.Resaler;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.test.UnitTest;

import java.math.BigDecimal;
import java.util.List;

/**
 * 测试余额正确性检查的正确性。
 */
public class FinanceCheckJobTest extends UnitTest {

    Supplier supplier;
    Account supplierAccount;
    Resaler resaler;
    Account resalerAccount;
    Account platformIncomingAccount;

    @Before
    public void setUp() throws Exception {
        FactoryBoy.deleteAll();
        JobWithHistory.cleanLastBeginRunAtForTest();

        supplier = FactoryBoy.create(Supplier.class);
        resaler = FactoryBoy.create(Resaler.class);
        platformIncomingAccount = AccountUtil.getPlatformIncomingAccount(Operator.defaultOperator());
        platformIncomingAccount.amount = new BigDecimal(1000);
        platformIncomingAccount.save();
        supplierAccount = AccountUtil.getSupplierAccount(supplier.id, Operator.defaultOperator());
        resalerAccount = AccountUtil.getResalerAccount(resaler);
        resalerAccount.creditable = AccountCreditable.YES;
        resalerAccount.amount = BigDecimal.ZERO;
        resalerAccount.save();
    }

    @Test
    public void testNormal() throws Exception {
        TradeBill transferTrade = TradeUtil.transferTrade()
                .fromAccount(platformIncomingAccount)
                .toAccount(supplierAccount)
                .balancePaymentAmount(BigDecimal.TEN)
                .make();
        TradeUtil.success(transferTrade, "测试转账");
        platformIncomingAccount.refresh();
        resalerAccount.refresh();

        assertEquals(new BigDecimal("990.00"), platformIncomingAccount.amount);
        assertEquals(BigDecimal.TEN, supplierAccount.amount);

        List<AccountSequence> sequenceList = AccountSequence.findAll();
        assertEquals(2, sequenceList.size());

        // TODO: 使用changeAmount+cashBalance来修正数据似乎不对，需要重要检查算法.
        for (AccountSequence seq : sequenceList) {
            //修改seqence值，期望amount会变更
            Logger.info("seq:" + seq);
            if (seq.changeAmount.compareTo(BigDecimal.ZERO) > 0) {
                assertEquals(BigDecimal.TEN, seq.balance);
                seq.changeAmount = seq.changeAmount.add(BigDecimal.ONE);
                seq.cashBalance = new BigDecimal(11);
            } else {
                assertEquals(new BigDecimal(990), seq.balance);
                seq.changeAmount = seq.changeAmount.subtract(BigDecimal.ONE);
                seq.cashBalance = new BigDecimal(989);
            }
            seq.save();
        }
        supplierAccount.amount = new BigDecimal(20);
        supplierAccount.save();

        new FinanceCheckJob().doJob();

        for (AccountSequence seq : sequenceList) {
            seq.refresh();
            //修改seqence值，期望amount会变更
            if (seq.changeAmount.compareTo(BigDecimal.ZERO) > 0) {
                assertEquals(new BigDecimal(11).setScale(2), seq.changeAmount);
            } else {
                assertEquals(new BigDecimal(-11).setScale(2), seq.changeAmount);
            }
        }

        supplierAccount.refresh();
        assertEquals(new BigDecimal(11).setScale(2), supplierAccount.amount);

    }
}
