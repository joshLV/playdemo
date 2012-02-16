package models.accounts;

import javax.persistence.*;

import play.db.jpa.Model;

@Entity
@Table(name="accounts")
public class Accounts extends Model {
    public long uid;
    public String accountType;
    public float totalAmount;


    public Accounts(long uid, String accountType, float totalAmount){
        this.uid = uid;
        this.accountType = accountType;
        this.totalAmount = totalAmount;
    }
}
