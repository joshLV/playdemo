package models.accounts;

import com.uhuila.common.util.DateUtil;
import models.accounts.util.AccountUtil;
import models.accounts.util.SerialNumberUtil;
import models.accounts.util.TradeUtil;
import models.order.Prepayment;
import play.Logger;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 提现流水
 *
 * @author likang
 *         Date: 12-3-6
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
    public String userName;             //银行账户的开户姓名

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


    @Transient
    public String paymentExcelOutAt;

    @Transient
    public String RMB;            //人民币大写

    @Transient
    public String thousandWan;    //千万

    @Transient
    public String hundredWan;     //百万

    @Transient
    public String tenWan;         //十万

    @Transient
    public String wan;            //万

    @Transient
    public String thousand;       //千

    @Transient
    public String hundred;        //百

    @Transient
    public String ten;            //十

    @Transient
    public String yuan;           //元

    @Transient
    public String jiao;           //角

    @Transient
    public String fen;            //分

    @Transient
    public String applierName;

    @Transient
    public String salesName;   //销售专员名字

    /**
     * 操作人
     */
    public String operator;

    /**
     * 备注，拒绝时填写拒绝理由
     */
    public String comment;

    @Column(name = "account_name")
    public String accountName;        //帐户名称，如果是商户，则为商户的短名称,如果是门店，则为门店的名称

    /**
     * 申请提现.
     *
     * @param applier 申请人信息
     * @param account 申请提现的账户
     */
    public boolean apply(String applier, Account account, String accountName) {
        this.applier = applier;
        this.account = account;
        this.status = WithdrawBillStatus.APPLIED;
        this.appliedAt = new Date();
        this.serialNumber = SerialNumberUtil.generateSerialNumber();
        this.accountName = accountName;
        this.save();

        try {
            AccountUtil.addBalanceWithoutSavingSequence(account.getId(), this.amount.negate(), this.amount, BigDecimal.ZERO,
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

    public static WithdrawBill findByIdAndUser(Long id, Long userId, AccountType accountType) {
        return WithdrawBill.find("id = ? and account.uid = ? and account.accountType = ?",
                id, userId, accountType).first();
    }

    public boolean reject(String comment) {
        return reject(comment, null);
    }


    /**
     * 提现申请被拒绝
     *
     * @param comment 拒绝理由.
     */
    public boolean reject(String comment, String operator) {
        if (this.status != WithdrawBillStatus.APPLIED) {
            Logger.error("the withdraw bill has been processed already");
            return false;
        }

        try {
            AccountUtil.addBalanceWithoutSavingSequence(this.account.getId(), this.amount, this.amount.negate(), BigDecimal.ZERO,
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
        this.operator = operator;
        this.save();
        return true;
    }

    /**
     * 结算操作，返回结算详细记录的笔数.
     *
     * @param fee
     * @param comment
     * @param withdrawDate
     * @return
     */
    public int settle(BigDecimal fee, String comment, Date withdrawDate, Prepayment prepayment) {
        if (this.status != WithdrawBillStatus.APPLIED) {
            Logger.error("the withdraw bill has been processed already");
            return 0;
        }

        this.status = WithdrawBillStatus.SUCCESS;
        this.comment = comment;
        this.processedAt = new Date();
        this.fee = fee;
        this.save();


        if (prepayment == null) {
            create2TradeBill(amount, BigDecimal.ZERO, id);
        } else if (amount.compareTo(prepayment.getBalance()) <= 0) { //可结算金额小于或等于预付款余额时，产生一笔TradeBill，只产生预付款结算记录
            create2TradeBill(BigDecimal.ZERO, amount, id);
        } else {
            //如果预付款已过期
            final Date endOfPrepaymentExpireAt = DateUtil.getEndOfDay(prepayment.expireAt);

            if (withdrawDate.after(endOfPrepaymentExpireAt)) {
                BigDecimal cashSettledAmount = AccountSequence.getVostroAmount(account, endOfPrepaymentExpireAt);
                if (cashSettledAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    create2TradeBill(amount.subtract(prepayment.getBalance()), prepayment.getBalance(), id);
                } else {
                    //获取过期时间之前的所有的未结算的收入总额。
                    BigDecimal moreAmount = AccountSequence.getVostroAmountTo(account, endOfPrepaymentExpireAt);
                    moreAmount = moreAmount.compareTo(prepayment.getBalance()) > 0 ? moreAmount.subtract(prepayment.getBalance()) : BigDecimal.ZERO;
                    create2TradeBill(cashSettledAmount.add(moreAmount), amount.subtract(cashSettledAmount).subtract(moreAmount), id);
                }
            } else {
                //预付款未过期
                create2TradeBill(amount.subtract(prepayment.getBalance()), prepayment.getBalance(), id);
            }
        }

        if (account.accountType == AccountType.SUPPLIER || account.accountType == AccountType.SHOP) { //同意提现操作
            //标记所有详细销售记录为已结算
            return AccountSequence.settle(account, withdrawDate, this, prepayment);
        }
        return 0;
    }

    /**
     * 结算时生成TradeBill.
     *
     * @param cashSettledAmount
     * @param prepaymentSettledAmount
     */
    private void create2TradeBill(BigDecimal cashSettledAmount, BigDecimal prepaymentSettledAmount, Long withdrawBillId) {
        if (cashSettledAmount.compareTo(BigDecimal.ZERO) > 0) {
            TradeBill prepaymentTradeBill = TradeUtil.withdrawTrade(this.account, cashSettledAmount, withdrawBillId).make();

            TradeUtil.success(prepaymentTradeBill, "现金结算", SettlementStatus.CLEARED);
        }
        if (prepaymentSettledAmount.compareTo(BigDecimal.ZERO) > 0) {
            TradeBill cashPayTradeBill = TradeUtil.withdrawTrade(this.account, prepaymentSettledAmount, withdrawBillId).make();
            TradeUtil.success(cashPayTradeBill, "预付款结算", SettlementStatus.CLEARED);
        }
    }

    public int agree(BigDecimal fee, String comment, Date withdrawDate) {
        return agree(fee, comment, withdrawDate, null);
    }

    /**
     * 同意提现操作，返回结算详细记录的笔数.
     *
     * @param fee
     * @param comment
     * @param withdrawDate
     * @return
     */
    public int agree(BigDecimal fee, String comment, Date withdrawDate, String operator) {
        if (this.status != WithdrawBillStatus.APPLIED) {
            Logger.error("the withdraw bill has been processed already");
            return 0;
        }

        this.status = WithdrawBillStatus.SUCCESS;
        this.comment = comment;
        this.processedAt = new Date();
        this.fee = fee;
        this.operator = operator;
        this.save();

        TradeBill tradeBill = TradeUtil.withdrawTrade(this.account, this.amount, id).make();
        TradeUtil.success(tradeBill, "提现成功", SettlementStatus.CLEARED);

        //只有商户和门店 才处理相关的clearedAccount
        if (account.accountType == AccountType.SUPPLIER || account.accountType == AccountType.SHOP) {
            BigDecimal tempClearedAmount = BigDecimal.ZERO;
            List<ClearedAccount> clearedAccountList = ClearedAccount.find(
                    "accountId=? and settlementStatus=? and date < ? order by date",
                    account.id, SettlementStatus.UNCLEARED, this.appliedAt).fetch();
            for (ClearedAccount clearedAccount : clearedAccountList) {
                tempClearedAmount = tempClearedAmount.add(clearedAccount.amount);
                clearedAccount.settlementStatus = SettlementStatus.CLEARED;
                clearedAccount.withdrawBill = this;
                clearedAccount.updatedBy = operator;
                clearedAccount.updatedAt = new Date();
                clearedAccount.save();
                //如果累计可提现审批金额超过提现金额，则创建一条clearedAccount,记录两者差额
                if (tempClearedAmount.compareTo(this.amount) > 0) {
                    ClearedAccount addClearedAccount = new ClearedAccount();
                    addClearedAccount.settlementStatus = SettlementStatus.UNCLEARED;
                    addClearedAccount.accountId = account.id;
                    addClearedAccount.amount = tempClearedAmount.subtract(this.amount);
                    addClearedAccount.withdrawBill = this;
                    addClearedAccount.date = clearedAccount.date;
                    addClearedAccount.save();
                    break;
                }
                if (tempClearedAmount.compareTo(this.amount) == 0) {
                    break;
                }
            }
            return 1;
        }
//        if (account.accountType == AccountType.SUPPLIER || account.accountType == AccountType.SHOP) { //同意提现操作
//            标记所有详细销售记录为已结算
//            return AccountSequence.withdraw(account, withdrawDate, this);
//        }
        return 0;
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

    /**
     * 获取已提现的总金额.
     *
     * @param account
     * @return
     */
    public static BigDecimal getWithdrawAmount(Account account) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select sum(b.amount) from WithdrawBill b where account=:account and status=:status");
        q.setParameter("account", account);
        q.setParameter("status", WithdrawBillStatus.SUCCESS);

        return q.getSingleResult() == null ? BigDecimal.ZERO : (BigDecimal) q.getSingleResult();
    }

    /**
     * 获取待审批的提现总金额.
     */
    public static BigDecimal getApplyingAmount(Account account) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select sum(b.amount) from WithdrawBill b where account=:account and status=:status");
        q.setParameter("account", account);
        q.setParameter("status", WithdrawBillStatus.APPLIED);

        return q.getSingleResult() == null ? BigDecimal.ZERO : (BigDecimal) q.getSingleResult();
    }

    /**
     * 获取指定时间点后的待审批的提现总金额.
     */
    public static BigDecimal getApplyingAmountFrom(Account account, Date appliedAtAfter) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select sum(b.amount) from WithdrawBill b where account=:account and status=:status and appliedAt>:appliedAt");
        q.setParameter("account", account);
        q.setParameter("status", WithdrawBillStatus.APPLIED);
        q.setParameter("appliedAt", appliedAtAfter);

        return q.getSingleResult() == null ? BigDecimal.ZERO : (BigDecimal) q.getSingleResult();
    }


}
