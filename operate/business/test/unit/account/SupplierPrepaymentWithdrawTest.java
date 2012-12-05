package unit.account;

import static models.accounts.AccountType.SUPPLIER;
import static util.DateHelper.afterDays;
import static util.DateHelper.afterMinuts;
import static util.DateHelper.beforeDays;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountType;
import models.accounts.SettlementStatus;
import models.accounts.TradeBill;
import models.accounts.WithdrawAccount;
import models.accounts.WithdrawBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.order.Prepayment;
import models.sales.Goods;
import models.supplier.Supplier;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;

import com.uhuila.common.util.DateUtil;

import controllers.OperateRbac;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

/**
 * 使用了预付款的商户提现测试.
 *
 * @author tanglq
 */
public class SupplierPrepaymentWithdrawTest extends UnitTest {

    Supplier supplier;
    Goods goods;
    Account supplierAccount;
    WithdrawAccount withdrawAccount;
    
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        supplier = FactoryBoy.create(Supplier.class);
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.originalPrice = new BigDecimal(20);
                g.salePrice = new BigDecimal(25);
            }
        });
        supplierAccount = AccountUtil.getAccount(supplier.id, AccountType.SUPPLIER);
        withdrawAccount = FactoryBoy.create(WithdrawAccount.class);
        Account incomingAccount = AccountUtil.getPlatformIncomingAccount();
        incomingAccount.amount = new BigDecimal(10000);
        incomingAccount.save();
    }

    @Test
    public void 预付款足额消费_无结算_显示金额测试() {
        assertBigDecimalEquals(BigDecimal.ZERO, 账户余额());
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(beforeDays(21)));
        assertBigDecimalEquals(BigDecimal.ZERO, 预付款余额());

        创建预付款(beforeDays(20), afterDays(1), new BigDecimal(100));

        assertBigDecimalEquals(BigDecimal.ZERO, 账户余额());
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(beforeDays(19)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());

        产生一笔20元消费记录(beforeDays(18));  // 20元
        assertBigDecimalEquals(new BigDecimal(20), 账户余额());
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(beforeDays(17)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());

        产生一笔20元消费记录(beforeDays(16));  // 40元
        assertBigDecimalEquals(new BigDecimal(40), 账户余额());
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(beforeDays(15)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());

        产生多笔20元消费记录(beforeDays(14), 3);  //  100元
        assertBigDecimalEquals(new BigDecimal(100), 账户余额());
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(beforeDays(9)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());

        产生一笔20元消费记录(beforeDays(8));   // 120元
        assertBigDecimalEquals(new BigDecimal(120), 账户余额());
        assertBigDecimalEquals(new BigDecimal(120), 可结算余额(beforeDays(7)));
        assertBigDecimalEquals(new BigDecimal(20), 可提现金额(beforeDays(7)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());
   
    }

    @Test
    public void 预付款未完成消费_无结算_显示金额测试() {
        创建预付款(beforeDays(20), beforeDays(10), new BigDecimal(100));
        assertBigDecimalEquals(BigDecimal.ZERO, 账户余额());
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(beforeDays(19)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());

        产生一笔20元消费记录(beforeDays(18));  // 20元
        assertBigDecimalEquals(new BigDecimal(20), 账户余额());
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(beforeDays(17)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());

        产生一笔20元消费记录(beforeDays(16));  // 40元
        assertBigDecimalEquals(new BigDecimal(40), 账户余额());
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(beforeDays(15)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());

        产生一笔20元消费记录(beforeDays(14));  //  60元

        // 过截止期未消费完成
        产生一笔20元消费记录(beforeDays(8));  // 80元
        assertBigDecimalEquals(new BigDecimal(80), 账户余额());
        assertBigDecimalEquals(new BigDecimal(20), 可提现金额(beforeDays(7)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());

        产生一笔20元消费记录(beforeDays(6));   // 100元
        assertBigDecimalEquals(new BigDecimal(100), 账户余额());
        assertBigDecimalEquals(new BigDecimal(40), 可提现金额(beforeDays(5)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());
    }

    @Test
    public void 预付款未完成消费_有效期后结算() {
        创建预付款(beforeDays(20), beforeDays(10), new BigDecimal(100));
        assertBigDecimalEquals(BigDecimal.ZERO, 账户余额());
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(beforeDays(19)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());

        产生一笔20元消费记录(beforeDays(18));  // 20元
        assertBigDecimalEquals(new BigDecimal(20), 账户余额());
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(beforeDays(17)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());

        产生一笔20元消费记录(beforeDays(16));  // 40元
        assertBigDecimalEquals(new BigDecimal(40), 账户余额());
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(beforeDays(15)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());

        产生一笔20元消费记录(beforeDays(14));  //  60元

        // 过截止期未消费完成
        产生一笔20元消费记录(beforeDays(8));  // 80元
        assertBigDecimalEquals(new BigDecimal(80), 账户余额());
        assertBigDecimalEquals(new BigDecimal(20), 可提现金额(beforeDays(7)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());

        产生一笔20元消费记录(beforeDays(6));   // 100元
        assertBigDecimalEquals(new BigDecimal(100), 账户余额());
        assertBigDecimalEquals(new BigDecimal(40), 可提现金额(beforeDays(5)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());
    }

    @Test
    public void 预付款未完成消费_有效期内结算() {
        创建预付款(beforeDays(20), beforeDays(10), new BigDecimal(100));
        assertBigDecimalEquals(BigDecimal.ZERO, 账户余额());
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(beforeDays(19)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());

        产生一笔20元消费记录(beforeDays(18));  // 20元
        assertBigDecimalEquals(new BigDecimal(20), 账户余额());
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(beforeDays(17)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());

        产生一笔20元消费记录(beforeDays(16));  // 40元
        assertBigDecimalEquals(new BigDecimal(40), 账户余额());
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(beforeDays(15)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());

        产生一笔20元消费记录(beforeDays(14));  //  60元

        // 过截止期未消费完成
        产生一笔20元消费记录(beforeDays(8));  // 80元
        assertBigDecimalEquals(new BigDecimal(80), 账户余额());
        assertBigDecimalEquals(new BigDecimal(20), 可提现金额(beforeDays(7)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());

        产生一笔20元消费记录(beforeDays(6));   // 100元
        assertBigDecimalEquals(new BigDecimal(100), 账户余额());
        assertBigDecimalEquals(new BigDecimal(40), 可提现金额(beforeDays(5)));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额());
    }


    private BigDecimal 账户余额() {
        return AccountUtil.getSupplierAccount(supplier.id).amount;
    }

    private BigDecimal 可提现金额(Date date) {
        Prepayment lastPrepayment = Prepayment.getLastUnclearedPrepayments(supplier.id);

        return Supplier.getWithdrawAmount(AccountUtil.getSupplierAccount(supplier.id), lastPrepayment, 可结算余额(date), date);
    }

    private BigDecimal 可结算余额(Date date) {
        // T+1，返回前一天的账户余额
        return supplierAccount.getWithdrawAmount(DateUtil.getBeginOfDay(date));
    }

    private BigDecimal 预付款余额() {
        Prepayment lastPrepayment = Prepayment.getLastUnclearedPrepayments(supplier.id);
        return lastPrepayment == null ? BigDecimal.ZERO : lastPrepayment.getBalance();
    }

    private void 结算预付款(Prepayment prepayment, BigDecimal amount, Date withdrawDate) {
        BigDecimal fee = BigDecimal.ZERO;
        
        //生成结算账单
        WithdrawBill bill = new WithdrawBill();
        if (withdrawAccount != null) {
            bill.userName = withdrawAccount.userName;
            bill.bankCity = withdrawAccount.bankCity;
            bill.bankName = withdrawAccount.bankName;
            bill.subBankName = withdrawAccount.subBankName;
            bill.cardNumber = withdrawAccount.cardNumber;
        }
        bill.amount = amount;
        bill.fee = fee == null ? BigDecimal.ZERO : fee;
        Supplier supplier = Supplier.findById(supplierAccount.uid);
        //申请提现
        bill.apply(OperateRbac.currentUser().userName, supplierAccount, supplier.otherName);

        //结算
        int withdrawCount = bill.settle(fee, "通过提现", withdrawDate, prepayment);
        if (withdrawCount > 0 && prepayment != null) {
            //将结算金额与预付款金额进行绑定
            if (prepayment != null && prepayment.getBalance().compareTo(BigDecimal.ZERO) >= 0) {
                boolean payAll = Prepayment.pay(prepayment, bill.amount);
            }
        }
    }

    public void assertBigDecimalEquals(BigDecimal expect, BigDecimal value) {
        assertEquals("Expect " + expect + " but was " + value,
                expect.setScale(4), value.setScale(4));
    }

    private Prepayment 创建预付款(final Date effectiveAt, final Date expireAt, final BigDecimal prepaymentAmount) {
        return FactoryBoy.create(Prepayment.class, new BuildCallback<Prepayment>() {
            @Override
            public void build(Prepayment p) {
                p.effectiveAt = effectiveAt;
                p.expireAt = expireAt;
                p.createdAt = effectiveAt;
                p.amount = prepaymentAmount;
                p.settlementStatus = SettlementStatus.UNCLEARED;
                p.supplier = supplier;
            }
        });
    }

    private void 产生一笔20元消费记录(final Date date) {
        产生多笔20元消费记录(date, 1);
    }

    private void 产生多笔20元消费记录(final Date date, int number) {
        for (int i = 0; i < number; i++) {
            final int afterMinuts = i;
            FactoryBoy.create(Order.class);
            FactoryBoy.create(OrderItems.class);
            ECoupon ecoupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
                @Override
                public void build(ECoupon e) {
                    e.status = ECouponStatus.CONSUMED;
                    e.consumedAt = afterMinuts(date, afterMinuts);
                }
            });

            // 给商户打钱
            TradeBill consumeTrade = TradeUtil.createConsumeTrade(ecoupon.eCouponSn,
                    supplierAccount, ecoupon.originalPrice, ecoupon.order.getId());
            consumeTrade.createdAt = afterMinuts(date, afterMinuts);
            TradeUtil.success(consumeTrade, "券消费(" + ecoupon.order.description + ")");

            List<AccountSequence> accountSequences = AccountSequence.find("tradeId=?", consumeTrade.id).fetch();
            for (AccountSequence accountSequence : accountSequences) {
                accountSequence.createdAt = consumeTrade.createdAt;
                accountSequence.save();
            }
        }
    }
}
