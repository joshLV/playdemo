package models.accounts;

import models.operator.Operator;
import org.apache.commons.lang.builder.ToStringBuilder;
import play.Logger;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
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

    /**
     * 商户标识.
     * 仅当帐号是按门店结算的帐号时该字段不为空
     */
    @Column(name = "supplier_id")
    public Long supplierId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type")
    public AccountType accountType;         //账户类型

    @Enumerated(EnumType.STRING)
    public AccountCreditable creditable;    //是否可赊账 即账户中余额是否允许小于0, 若否, 当余额小于0时会抛异常

    @Column(name = "amount")
    public BigDecimal amount = BigDecimal.ZERO;               //可支付、可提现余额，现金余额，对应account_sequence中的cash_balance

    @Column(name = "promotion_amount")
    public BigDecimal promotionAmount = BigDecimal.ZERO;      //可支付、不可提现余额，对应account_sequence中的promotion_balance

    @Column(name = "uncash_amount")
    public BigDecimal uncashAmount = BigDecimal.ZERO;         //不可支付、不可提现余额,申请提现待审批的，对应account_sequence中的uncash_balance

    @Enumerated(EnumType.STRING)
    public AccountStatus status;            //账户状态

    @Column(name = "created_at")
    public Date createdAt;

    @ManyToOne
    public Operator operator;

    @Transient
    public String info;

    @Version
    @Column(name = "lock_version")
    public int lockVersion;

    public static final long PLATFORM_INCOMING = 1L;   //券平台收款账户
    public static final long PLATFORM_COMMISSION = 2L;   //券平台佣金账户
    public static final long UHUILA_COMMISSION = 3L;   //一百券佣金账户
    public static final long PLATFORM_WITHDRAW = 4L;   //提现账户,用户提现金额都会汇总到这里
    public static final long FINANCING_INCOMING = 5L;   //财务收款虚拟账户,收款后将款项打给具体账户
    public static final long PROMOTION = 6L;   //可支付、不可提现金额的系统出款账户

    public static final long PARTNER_ALIPAY = 100L; //支付宝虚拟账户
    public static final long PARTNER_TENPAY = 101L; //财付通虚拟账户
    public static final long PARTNER_KUAIQIAN = 102L; //快钱虚拟账户
    public static final long PARTNER_SINA = 103L; //快钱虚拟账户


    public Account() {

    }

    public Account(long uid, AccountType type) {
        this(uid, type, Operator.defaultOperator());
    }

    public Account(long uid, AccountType type, Operator operator) {
        this.uid = uid;
        this.accountType = type;
        this.amount = BigDecimal.ZERO;
        this.uncashAmount = BigDecimal.ZERO;
        this.status = AccountStatus.NORMAL;
        this.createdAt = new Date();
        this.creditable = AccountCreditable.NO;
        this.operator = operator;
    }

    /**
     * 可结算金额.
     * 从指定日期到以前所有的未结算过的可结算金额.
     * 可结算金额=账户余额-指定日期之后消费总额
     */
    public BigDecimal getWithdrawAmount(Date date) {
        BigDecimal vostroAmount = AccountSequence.getVostroAmountTo(this, date);
        Logger.info("Account.getWithdrawAmount vostroAmount=" + vostroAmount + ", uncashAmount=" + uncashAmount);
        if (uncashAmount == null) {
            return vostroAmount == null ? BigDecimal.ZERO : vostroAmount;
        }
        return vostroAmount.subtract(uncashAmount);
    }

    /**
     * 账户可提现余额
     *
     * @param prepaymentBalance
     * @param date
     * @return
     */
    public BigDecimal getSupplierWithdrawAmount(BigDecimal prepaymentBalance, Date date) {
        //本周期券消费金额  本周期可以提现的金额
        BigDecimal withdrawAmount = getWithdrawAmount(date);
        if (prepaymentBalance.compareTo(withdrawAmount) > 0) {
            return BigDecimal.ZERO;
        }
        //剩余未提现金额
        return withdrawAmount.subtract(prepaymentBalance);
    }

    public boolean isCreditable() {
        return this.creditable == AccountCreditable.YES;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("uid", uid)
                .append("accountType", accountType)
                .append("creditable", creditable)
                .append("amount", amount)
                .append("operator", operator)
                .append("status", status)
                .toString();
    }
}
