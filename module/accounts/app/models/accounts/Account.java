package models.accounts;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import play.db.jpa.Model;

@Entity
@Table(name="accounts")
public class Account extends Model {
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
    public Date createAt;

    public Account(long uid, AccountType type){
        this.uid = uid;
        this.type = type;
        this.amount = new BigDecimal(0);
        this.cashAmount = new BigDecimal(0);
        this.uncashAmount = new BigDecimal(0);
        this.status = AccountStatus.NORMAL;
        this.createAt = new Date();
    }

    public Account addCash(BigDecimal augend){
        if ( this.cashAmount.add(augend).compareTo(BigDecimal.ZERO) >= 0 ){
            this.cashAmount = this.cashAmount.add(augend);
            this.amount = this.amount.add(augend);
        }
        return this;
    }

    public Account addUncash(BigDecimal augend){
        if( this.uncashAmount.add(augend).compareTo(BigDecimal.ZERO) >= 0){
            this.uncashAmount = this.uncashAmount.add(augend);
            this.amount = this.amount.add(augend);
        }
        return this;
    }
}
