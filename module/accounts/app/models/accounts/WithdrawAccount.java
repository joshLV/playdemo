package models.accounts;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

/**
 * 预设的提现账户，用于运营人员给分销商或者商家设置提现账户
 *
 * @author likang
 *         Date: 12-5-8
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

    @Column(name = "supplier_id")
    public Long supplierId;

    @Transient
    public Long shopId;

    public static List<WithdrawAccount> findByShop(Long shopId) {
        return find("byUserIdAndAccountType", shopId, AccountType.SHOP).fetch();
    }

    public static List<WithdrawAccount> findByUser(Long userId, AccountType accountType) {
        return find("byUserIdAndAccountType", userId, accountType).fetch();
    }

    public static WithdrawAccount findByIdAndUser(long id, Long userId, AccountType accountType) {
        return find("byIdAndUserIdAndAccountType", id, userId, accountType).first();
    }

    public static List<WithdrawAccount> findAllBySupplier(long supplierId) {
        List<WithdrawAccount> withdrawAccounts = find("(userId=? and accountType=?) or supplierId = ? ", supplierId, AccountType.SUPPLIER, supplierId).fetch();
        for (WithdrawAccount withdrawAccount : withdrawAccounts) {
            if (withdrawAccount.accountType == AccountType.SHOP){
                withdrawAccount.shopId = withdrawAccount.userId;
            }
        }
        return withdrawAccounts;
    }

}
