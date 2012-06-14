package models.accounts;

import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "accounts")
public class Account extends Model {
    public long uid;                        //用户ID, 与accountType 一起可做唯一约束

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type")
    public AccountType accountType;         //账户类型

    @Enumerated(EnumType.STRING)
    public AccountCreditable creditable;    //是否可赊账 即账户中余额是否允许小于0, 若否, 当余额小于0时会抛异常

    public BigDecimal amount;               //可提现余额

    @Column(name = "uncash_amount")
    public BigDecimal uncashAmount;         //不可提现余额

    @Enumerated(EnumType.STRING)
    public AccountStatus status;            //账户状态

    @Column(name = "created_at")
    public Date createdAt;

    @Transient
    public String info;

    public static final long PLATFORM_INCOMING      = 1L;   //券平台收款账户
    public static final long PLATFORM_COMMISSION    = 2L;   //券平台佣金账户
    public static final long UHUILA_COMMISSION      = 3L;   //一百券佣金账户
    public static final long PLATFORM_WITHDRAW      = 4L;   //提现账户,用户提现金额都会汇总到这里
    public static final long FINANCING_INCOMING     = 5L;   //财务收款虚拟账户,收款后将款项打给具体账户

    public static final long PARTNER_ALIPAY         = 100L; //支付宝虚拟账户
    public static final long PARTNER_TENPAY         = 101L; //财付通虚拟账户
    public static final long PARTNER_KUAIQIAN       = 102L; //快钱虚拟账户

    public Account() {

    }

    public Account(long uid, AccountType type) {
        this.uid = uid;
        this.accountType = type;
        this.amount = new BigDecimal(0);
        this.uncashAmount = new BigDecimal(0);
        this.status = AccountStatus.NORMAL;
        this.createdAt = new Date();
        this.creditable = AccountCreditable.NO;
    }

    public boolean isCreditable(){
        return this.creditable == AccountCreditable.YES;
    }

}
