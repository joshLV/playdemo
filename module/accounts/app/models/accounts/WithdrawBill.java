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
    public void apply(Account account){
        this.account = account;
        this.status = WithdrawBillStatus.APPLIED;
        this.appliedAt = new Date();
        this.serialNumber = SerialNumberUtil.generateSerialNumber();
        this.save();

        AccountUtil.addBalance(account.getId(),this.amount.negate(), this.amount,
                this.getId(),AccountSequenceType.FREEZE,"申请提现");
    }

    /**
     * 提现申请被拒绝
     *
     * @param comment 拒绝理由.
     */
    public void reject(String comment){
        if(this.status != WithdrawBillStatus.APPLIED){
            throw new RuntimeException("The withdraw request has been processed");
        }

        AccountUtil.addBalance(this.account.getId(), this.amount, this.amount.negate(),
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
    public void agree(String comment){
        if(this.status != WithdrawBillStatus.APPLIED){
            throw new RuntimeException("The withdraw request has been processed");
        }

        AccountUtil.addBalance(this.account.getId(), BigDecimal.ZERO, this.amount.negate(),
                this.getId(), AccountSequenceType.WITHDRAW, "提现成功");

        this.status = WithdrawBillStatus.SUCCESS;
        this.comment = comment;
        this.save();
    }

}
