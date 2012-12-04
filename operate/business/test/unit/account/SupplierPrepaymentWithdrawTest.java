package unit.account;

import com.uhuila.common.util.DateUtil;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.TradeBill;
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
import org.junit.Ignore;
import org.junit.Test;
import play.test.UnitTest;

import java.math.BigDecimal;
import java.util.Date;

import static util.DateHelper.afterDays;
import static util.DateHelper.beforeDays;

/**
 * 使用了预付款的商户提现测试.
 *
 * @author tanglq
 */
public class SupplierPrepaymentWithdrawTest extends UnitTest {

    Supplier supplier;
    Goods goods;
    Account supplierAccount;

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
    }

    @Ignore
    @Test
    public void 预付款足额消费状态测试() {
        assertBigDecimalEquals(BigDecimal.ZERO, 账户余额(supplier));
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(supplier, null, supplierAccount.getWithdrawAmount(DateUtil.getBeginOfDay()), DateUtil.getBeginOfDay()));
//todo        assertBigDecimalEquals(BigDecimal.ZERO, 预付款余额(supplier));

        Prepayment prepayment = 创建预付款(beforeDays(20), afterDays(1), new BigDecimal(100));

        assertBigDecimalEquals(BigDecimal.ZERO, 账户余额(supplier));
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(supplier, prepayment, supplierAccount.getWithdrawAmount(DateUtil.getBeginOfDay()), DateUtil.getBeginOfDay()));
//todo        assertBigDecimalEquals(new BigDecimal(100), 预付款余额(supplier));

        产生一笔20元消费记录(beforeDays(18));  // 20元
        assertBigDecimalEquals(new BigDecimal(20), 账户余额(supplier));
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(supplier, prepayment, supplierAccount.getWithdrawAmount(DateUtil.getBeginOfDay()), DateUtil.getBeginOfDay()));
//todo        assertBigDecimalEquals(new BigDecimal(100), 预付款余额(supplier));

        产生一笔20元消费记录(beforeDays(16));  // 40元
        assertBigDecimalEquals(new BigDecimal(40), 账户余额(supplier));
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(supplier, prepayment, supplierAccount.getWithdrawAmount(DateUtil.getBeginOfDay()), DateUtil.getBeginOfDay()));
//todo        assertBigDecimalEquals(new BigDecimal(100), 预付款余额(supplier));

        产生一笔20元消费记录(beforeDays(14));  //  60元
        产生一笔20元消费记录(beforeDays(12));  //  80元
        产生一笔20元消费记录(beforeDays(10));  // 100元
        assertBigDecimalEquals(new BigDecimal(100), 账户余额(supplier));
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(supplier, prepayment, supplierAccount.getWithdrawAmount(DateUtil.getBeginOfDay()), DateUtil.getBeginOfDay()));
//todo        assertBigDecimalEquals(new BigDecimal(100), 预付款余额(supplier));

        产生一笔20元消费记录(beforeDays(8));   // 120元
        assertBigDecimalEquals(new BigDecimal(120), 账户余额(supplier));
        assertBigDecimalEquals(new BigDecimal(20), 可提现金额(supplier, prepayment, supplierAccount.getWithdrawAmount(DateUtil.getBeginOfDay()), DateUtil.getBeginOfDay()));
//todo        assertBigDecimalEquals(new BigDecimal(100), 预付款余额(supplier));

        //assertBigDecimalEquals(new BigDecimal(20), supplierAccount.getSupplierWithdrawAmount(new BigDecimal(100), beforeDays(7)));       
    }

    @Ignore
    @Test
    public void 预付款未完成消费状态测试() {
        Prepayment prepayment = 创建预付款(beforeDays(20), beforeDays(10), new BigDecimal(100));
        assertBigDecimalEquals(BigDecimal.ZERO, 账户余额(supplier));
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(supplier, prepayment, supplierAccount.getWithdrawAmount(DateUtil.getBeginOfDay()), DateUtil.getBeginOfDay()));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额(prepayment));

        产生一笔20元消费记录(beforeDays(18));  // 20元
        assertBigDecimalEquals(new BigDecimal(20), 账户余额(supplier));
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(supplier, prepayment, supplierAccount.getWithdrawAmount(DateUtil.getBeginOfDay()), DateUtil.getBeginOfDay()));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额(prepayment));

        产生一笔20元消费记录(beforeDays(16));  // 40元
        assertBigDecimalEquals(new BigDecimal(40), 账户余额(supplier));
        assertBigDecimalEquals(BigDecimal.ZERO, 可提现金额(supplier, prepayment, supplierAccount.getWithdrawAmount(DateUtil.getBeginOfDay()), DateUtil.getBeginOfDay()));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额(prepayment));

        产生一笔20元消费记录(beforeDays(14));  //  60元

        // 过截止期未消费完成

        产生一笔20元消费记录(beforeDays(8));  // 80元
        assertBigDecimalEquals(new BigDecimal(80), 账户余额(supplier));
        assertBigDecimalEquals(new BigDecimal(20), 可提现金额(supplier, prepayment, supplierAccount.getWithdrawAmount(DateUtil.getBeginOfDay()), DateUtil.getBeginOfDay()));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额(prepayment));

        产生一笔20元消费记录(beforeDays(6));   // 100元
        assertBigDecimalEquals(new BigDecimal(100), 账户余额(supplier));
        assertBigDecimalEquals(new BigDecimal(40), 可提现金额(supplier, prepayment, supplierAccount.getWithdrawAmount(DateUtil.getBeginOfDay()), DateUtil.getBeginOfDay()));
        assertBigDecimalEquals(new BigDecimal(100), 预付款余额(prepayment));
    }


    private BigDecimal 账户余额(Supplier supplier) {
        return AccountUtil.getSupplierAccount(supplier.id).amount;
    }

    private BigDecimal 可提现金额(Supplier supplier, Prepayment prepayment, BigDecimal withdrawAmount, Date date) {
        return Supplier.getWithdrawAmount(AccountUtil.getSupplierAccount(supplier.id), prepayment, withdrawAmount, date);
    }

    private BigDecimal 预付款余额(Prepayment prepayment) {
        return prepayment.getBalance();
    }

    private WithdrawBill 申请提现(BigDecimal amount) {
        return null;
    }

    private void 通过提现(WithdrawBill bill) {

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
            }
        });
    }

    private void 产生一笔20元消费记录(final Date date) {
        FactoryBoy.create(Order.class);
        FactoryBoy.create(OrderItems.class);
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon e) {
                e.status = ECouponStatus.CONSUMED;
                e.consumedAt = date;
            }
        });

        // 给商户打钱
        TradeBill consumeTrade = TradeUtil.createConsumeTrade(ecoupon.eCouponSn,
                supplierAccount, ecoupon.originalPrice, ecoupon.order.getId());
        consumeTrade.createdAt = date;
        TradeUtil.success(consumeTrade, "券消费(" + ecoupon.order.description + ")");
    }
}
