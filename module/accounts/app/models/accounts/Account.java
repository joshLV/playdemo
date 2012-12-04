package models.accounts;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 现金账户
 *
 * @author likang
 */
@Entity
@Table(name = "accounts")
public class Account extends Model {
    public long uid;                        //用户ID, 与accountType 一起可做唯一约束

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type")
    public AccountType accountType;         //账户类型

    @Enumerated(EnumType.STRING)
    public AccountCreditable creditable;    //是否可赊账 即账户中余额是否允许小于0, 若否, 当余额小于0时会抛异常

    @Column(name = "amount")
    public BigDecimal amount;               //可支付、可提现余额

    @Column(name = "promotion_amount")
    public BigDecimal promotionAmount;      //可支付、不可提现余额

    @Column(name = "uncash_amount")
    public BigDecimal uncashAmount;         //不可支付、不可提现余额,申请提现待审批的

    @Enumerated(EnumType.STRING)
    public AccountStatus status;            //账户状态

    @Column(name = "created_at")
    public Date createdAt;

    @Transient
    public String info;

    public static final long PLATFORM_INCOMING = 1L;   //券平台收款账户
    public static final long PLATFORM_COMMISSION = 2L;   //券平台佣金账户
    public static final long UHUILA_COMMISSION = 3L;   //一百券佣金账户
    public static final long PLATFORM_WITHDRAW = 4L;   //提现账户,用户提现金额都会汇总到这里
    public static final long FINANCING_INCOMING = 5L;   //财务收款虚拟账户,收款后将款项打给具体账户
    public static final long PROMOTION = 6L;   //可支付、不可提现金额的系统出款账户

    public static final long PARTNER_ALIPAY = 100L; //支付宝虚拟账户
    public static final long PARTNER_TENPAY = 101L; //财付通虚拟账户
    public static final long PARTNER_KUAIQIAN = 102L; //快钱虚拟账户


    public Account() {

    }

    public Account(long uid, AccountType type) {
        this.uid = uid;
        this.accountType = type;
        this.amount = BigDecimal.ZERO;
        this.uncashAmount = BigDecimal.ZERO;
        this.status = AccountStatus.NORMAL;
        this.createdAt = new Date();
        this.creditable = AccountCreditable.NO;
    }

    /**
     * 可结算金额.
     * 从指定日期到以前所有的未结算过的可结算金额.
     * 可结算金额=账户余额-指定日期之后消费总额
     */
    @Transient
    public BigDecimal getWithdrawAmount(Date date) {
        BigDecimal todayWithdrawAmount = AccountSequence.getVostroAmount(this, date);
        System.out.println("todayWithdrawAmount:" + todayWithdrawAmount);
        return (amount.compareTo(todayWithdrawAmount) <= 0) ? BigDecimal.ZERO : amount.subtract(todayWithdrawAmount);

//        BigDecimal incomeAmount = AccountSequence.getIncomeAmount(this, date).subtract(todayWithdrawAmount);
//
//        if (uncashAmount == null) {
//            return incomeAmount == null ? BigDecimal.ZERO : incomeAmount;
//        }
//        if (incomeAmount.compareTo(uncashAmount) <= 0) {
//            return BigDecimal.ZERO;
//        }
//
//        return incomeAmount.subtract(uncashAmount);
    }

    /**
     * 账户可提现余额
     *
     * @param prepaymentBalance
     * @param date
     * @return
     */
    public BigDecimal getSupplierWithdrawAmount(BigDecimal prepaymentBalance, Date date) {
        BigDecimal withdrawAmount = getWithdrawAmount(date);
        if (prepaymentBalance.compareTo(withdrawAmount) > 0) {
            return BigDecimal.ZERO;
        }
        return withdrawAmount.subtract(prepaymentBalance);
    }

    public boolean isCreditable() {
        return this.creditable == AccountCreditable.YES;
    }

    public static Account getConsumer(Long uid) {
        return Account.find("byUidAndAccountType", uid, AccountType.CONSUMER).first();
    }

    public static Account getSupplier(Long uid) {
        return Account.find("byUidAndAccountType", uid, AccountType.SUPPLIER).first();
    }

}
