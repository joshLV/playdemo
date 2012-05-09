package models.accounts;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

/**
 * @author likang
 * Date: 12-5-8
 */
@Entity
@Table(name = "withdraw_account")
public class WithdrawAccount extends Model {

    @Column(name = "user_id")
    public Long userId;

    @Column(name = "account_type")
    @Enumerated(EnumType.STRING)
    public AccountType accountType;

    @Column(name = "user_name")
    @Required
    public String userName;

    @Column(name = "bank_city")
    @Required
    public String bankCity;

    @Column(name = "bank_name")
    @Required
    public String bankName;

    @Column(name = "sub_bank_name")
    @Required
    public String subBankName;

    @Column(name = "card_number")
    @Required
    public String cardNumber;

    public static List<WithdrawAccount> findByAccount(Long userId, AccountType accountType){
        return WithdrawAccount.find("byUserIdAndAccountType", userId, accountType).fetch();
    }

}
