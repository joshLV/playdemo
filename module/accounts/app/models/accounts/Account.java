package models.accounts;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import play.db.jpa.Model;

@Entity
@Table(name="accounts")
public class Account extends Model {
    public long uid;
    
    private static final long SHIHUI = 1L;
    private static final long UHUILA = 2L;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type")
    public AccountType accountType;        //账户类型

    public BigDecimal amount;       //总金额

    @Enumerated(EnumType.STRING)
    public AccountStatus status;

    @Column(name = "create_at")
    public Date createdAt;

    public Account(long uid, AccountType type){
        this.uid = uid;
        this.accountType = type;
        this.amount = new BigDecimal(0);
        this.status = AccountStatus.NORMAL;
        this.createdAt = new Date();
    }
    
    public static Account getUhuilaAccount(){
    	return getAccount(UHUILA, AccountType.PLATFORM);
    }
    
    public static Account getShihuiAccount(){
    	return getAccount(SHIHUI, AccountType.PLATFORM);
    }
    
    public static Account getAccount(long uid, AccountType type){
    	Account account = Account.find("byUidAndAccountType", uid, type).first();
    	if(account == null){
    		synchronized(Account.class){
    			account = Account.find("byUidAndAccountType", uid, type).first();
    			if(account == null){
    				return new Account(uid, type).save();
    			}
    		}
    	}
    	return account;
    }

}
