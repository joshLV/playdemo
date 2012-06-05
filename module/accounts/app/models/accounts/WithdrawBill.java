package models.accounts;

import models.accounts.util.AccountUtil;
import models.accounts.util.SerialNumberUtil;
import models.accounts.util.TradeUtil;
import play.Logger;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

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

    @Column(name = "applier")
    public String applier;

    @Required
    @Min(10)
    public BigDecimal amount;           //提现金额

    public BigDecimal fee;              //手续费

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

    @Enumerated(EnumType.STRING)
    public WithdrawBillStatus status;

    @Column(name = "applied_at")
    public Date appliedAt;

    @Column(name = "processed_at")
    public Date processedAt;

    public String comment;

    /**
     * 申请提现.
     * @param applier 申请人信息
     * @param account 申请提现的账户
     */
    public boolean apply(String applier, Account account){
        this.applier = applier;
        this.account = account;
        this.status = WithdrawBillStatus.APPLIED;
        this.appliedAt = new Date();
        this.serialNumber = SerialNumberUtil.generateSerialNumber();
        this.save();

        try {
            AccountUtil.addBalanceWithoutSavingSequence(account.getId(), this.amount.negate(), this.amount,
                    this.getId(), "申请提现", null);
        } catch (BalanceNotEnoughException e) {
            Logger.error(e, e.getMessage());
            return false;
        } catch (AccountNotFoundException e) {
            Logger.error(e, e.getMessage());
            return false;
        }
        return true;
    }

    public static WithdrawBill findByIdAndUser(Long id, Long userId, AccountType accountType){
        return WithdrawBill.find("id = ? and account.uid = ? and account.accountType = ?",
                id, userId, accountType).first();
    }

    /**
     * 提现申请被拒绝
     *
     * @param comment 拒绝理由.
     */
    public boolean reject(String comment){
        if(this.status != WithdrawBillStatus.APPLIED){
            Logger.error("the withdraw bill has been processed already");
            return false;
        }

        try {
            AccountUtil.addBalanceWithoutSavingSequence(this.account.getId(), this.amount, this.amount.negate(),
                    this.getId(), "拒绝提现", null);
        } catch (BalanceNotEnoughException e) {
            Logger.error(e, e.getMessage());
            return false;
        } catch (AccountNotFoundException e) {
            Logger.error(e, e.getMessage());
            return false;
        }

        this.comment = comment;
        this.status = WithdrawBillStatus.REJECTED;
        this.processedAt = new Date();
        this.save();
        return true;
    }

    /**
     * 退款提现成功
     *
     * @param comment 备注.
     */
    public boolean agree(BigDecimal fee, String comment){
        if(this.status != WithdrawBillStatus.APPLIED){
            Logger.error("the withdraw bill has been processed already");
            return false;
        }

        TradeBill tradeBill = TradeUtil.createWithdrawTrade(this.account, this.amount);
        TradeUtil.success(tradeBill, "提现成功");

        this.status = WithdrawBillStatus.SUCCESS;
        this.comment = comment;
        this.processedAt = new Date();
        this.fee = fee;
        this.save();
        return true;
    }


    public static JPAExtPaginator<WithdrawBill> findByCondition(
            WithdrawBillCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<WithdrawBill> page = new JPAExtPaginator<>(
                null, null, WithdrawBill.class, condition.getFilter(), condition.getParams());

        page.orderBy("appliedAt DESC");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

}
