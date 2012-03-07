package models.accounts;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import play.db.jpa.Model;

@Entity
@Table(name="accounts")
public class Account extends Model {
    private static final long uhuilaUid = 1;
    public long uid;

    @Enumerated(EnumType.STRING)
    public AccountType type;        //账户类型

    public BigDecimal amount;       //总金额

    @Column(name = "cash_amount")
    public BigDecimal cashAmount;   //可提现金额

    @Column(name = "uncash_amount")
    public BigDecimal uncashAmount; //不可提现金额

    @Enumerated(EnumType.STRING)
    public AccountStatus status;

    @Column(name = "create_at")
    public Date createdAt;

    public static Account getUhuilaAccount(){
        Account account =  Account.find("byUid", uhuilaUid).first();
        if(account == null){
            throw new RuntimeException("can not get uhuila account"); //未建立uhuila账户
        }
        return account;
    }

    public Account(long uid, AccountType type){
        this.uid = uid;
        this.type = type;
        this.amount = new BigDecimal(0);
        this.cashAmount = new BigDecimal(0);
        this.uncashAmount = new BigDecimal(0);
        this.status = AccountStatus.NORMAL;
        this.createdAt = new Date();
    }

}
