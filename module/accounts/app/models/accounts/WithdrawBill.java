package models.accounts;

import models.accounts.util.AccountUtil;
import models.accounts.util.SerialNumberUtil;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 提现流水
 *
 * @author likang
 * Date: 12-3-6
 */
@Entity
@Table(name = "withdraw_bill")
public class WithdrawBill extends Model {
    
    public String serialNumber;         
    
    @ManyToOne
    public Account account;

    @Required
    public BigDecimal amount;           //提现金额

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
    public String subBankName;

    @Column(name = "card_number")
    @Required
    public String cardNumber;

    @Enumerated(EnumType.STRING)
    public WithdrawBillStatus status;

    @Column(name = "applied_at")
    public Date appliedAt;

    @Column(name = "processed_at")
    public Date processedAt;

    public String comment;

    /**
     * 申请提现.
     *
     * @param account 申请提现的账户
     */
    public void applied(Account account){
        AccountUtil.addBalance(account,this.amount.negate(), this.amount,
                this.getId(),AccountSequenceType.FREEZE,"申请提现");

        this.account = account;
        this.status = WithdrawBillStatus.APPLIED;
        this.appliedAt = new Date();
        this.serialNumber = SerialNumberUtil.generateSerialNumber();
        this.save();
    }

    /**
     * 提现申请被拒绝
     *
     * @param comment 拒绝理由.
     */
    public void rejected(String comment){
        AccountUtil.addBalance(this.account, this.amount, this.amount.negate(),
                this.getId(), AccountSequenceType.UNFREEZE,"拒绝提现");

        this.comment = comment;
        this.status = WithdrawBillStatus.REJECTED;
        this.save();
    }

    /**
     * 退款提现成功
     *
     * @param comment 备注.
     */
    public void success(String comment){
        AccountUtil.addBalance(this.account, BigDecimal.ZERO, this.amount.negate(),
                this.getId(), AccountSequenceType.WITHDRAW, "提现成功");

        this.status = WithdrawBillStatus.SUCCESS;
        this.comment = comment;
        this.save();
    }

}
