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

    @Enumerated(EnumType.STRING)
    public AccountStatus status;

    @Column(name = "create_at")
    public Date createdAt;

    public Account(long uid, AccountType type){
        this.uid = uid;
        this.type = type;
        this.amount = new BigDecimal(0);
        this.status = AccountStatus.NORMAL;
        this.createdAt = new Date();
    }

}
