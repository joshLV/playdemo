package models.accounts;

import javax.persistence.*;
import java.math.BigDecimal;

import play.db.jpa.Model;

@Entity
@Table(name="accounts")
public class Accounts extends Model {
    public long uid;
    public String accountType;
    public BigDecimal totalAmount;


    public Accounts(long uid, String accountType, BigDecimal totalAmount){
        this.uid = uid;
        this.accountType = accountType;
        this.totalAmount = totalAmount;
    }
}
