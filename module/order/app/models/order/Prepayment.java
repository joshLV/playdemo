package models.order;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountType;
import models.accounts.ClearedAccount;
import models.accounts.SettlementStatus;
import models.accounts.TradeBill;
import models.accounts.TradeType;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.operator.Operator;
import models.supplier.Supplier;
import org.apache.commons.lang.time.DateUtils;
import play.data.validation.Max;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.ModelPaginator;
import play.modules.view_ext.annotation.Money;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 付给商户的预付款.
 * <p/>
 * User: sujie
 * Date: 11/22/12
 * Time: 11:44 AM
 */
@Entity
@Table(name = "prepayment")
public class Prepayment extends Model {
    @Required
    @ManyToOne
    public Supplier supplier;   //商户

    @Required
    @Money
    @Min(0.01)
    @Max(999999)
    public BigDecimal amount;   //金额

    @Column(name = "withdraw_amount")
    public BigDecimal withdrawAmount = BigDecimal.ZERO;   //已结算金额

    @Column(name = "effective_at")
    public Date effectiveAt;    //有效期开始时间

    @Column(name = "expire_at")
    public Date expireAt;       //有效期结束时间

    public String remark;       //备注

    @Column(name = "settle_remark")
    public String settleRemark;  //结算备注

    @Column(name = "created_at")
    public Date createdAt;      //创建时间

    @Column(name = "created_by")
    public String createdBy;    //创建人帐号

    @Column(name = "updated_at")
    public Date updatedAt;      //最后修改时间

    @Column(name = "updated_by")
    public String updatedBy;    //最后修改人帐号

    public DeletedStatus deleted = DeletedStatus.UN_DELETED;   //删除状态

    @Column(name = "settlement_status")
    @Enumerated(EnumType.STRING)
    public SettlementStatus settlementStatus = SettlementStatus.UNCLEARED;   //结算状态

    public Boolean warning = false;

    public static void update(Long id, Prepayment prepayment, String loginName) {
        Prepayment oldPrepayment = Prepayment.findById(id);
        if (prepayment == null) {
            return;
        }
        oldPrepayment.supplier = prepayment.supplier;
        oldPrepayment.amount = prepayment.amount;
        oldPrepayment.updatedAt = new Date();
        oldPrepayment.updatedBy = loginName;
        oldPrepayment.effectiveAt = prepayment.effectiveAt;
        oldPrepayment.expireAt = DateUtil.getEndOfDay(prepayment.expireAt);
        oldPrepayment.remark = prepayment.remark;
        if (oldPrepayment.settlementStatus == null) {
            oldPrepayment.settlementStatus = SettlementStatus.UNCLEARED;
        }
        oldPrepayment.save();
    }

    public static ModelPaginator<Prepayment> getPage(int pageNumber, int pageSize, Long supplierId) {
        ModelPaginator<Prepayment> page;
        if (supplierId != null) {
            page = new ModelPaginator<>(Prepayment.class, "deleted = ? and supplier.id=?", DeletedStatus.UN_DELETED,
                    supplierId).orderBy("createdAt desc");
        } else {
            page = new ModelPaginator<>(Prepayment.class, "deleted = ? ", DeletedStatus.UN_DELETED).orderBy("createdAt desc");
        }
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

    @Transient
    public boolean isExpired() {
        return expireAt != null && expireAt.before(new Date());
    }

    @Transient
    /**
     * 只有从未被结算过的预付款记录才允许修改和删除
     */
    public boolean canUpdate() {
        return this.settlementStatus == SettlementStatus.UNCLEARED;
    }

    public static Prepayment getLastUnclearedPrepayments(long uid) {
        return find("supplier.id=? and settlementStatus=? and deleted = ? order by createdAt DESC", uid, SettlementStatus.UNCLEARED, DeletedStatus.UN_DELETED).first();
    }

    public static BigDecimal getUnclearedBalance(long uid) {
        BigDecimal prepaymentAmount = find("select sum(amount)-sum(withdrawAmount) from Prepayment where supplier.id=? " +
                "and settlementStatus=? group by supplier", uid, SettlementStatus.UNCLEARED).first();
        return prepaymentAmount == null ? BigDecimal.ZERO : prepaymentAmount;
    }

    @Transient
    public BigDecimal getBalance() {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        if (withdrawAmount == null) {
            return amount;
        }
        return amount.subtract(withdrawAmount);
    }

    @Transient
    public BigDecimal getAvailableBalance(BigDecimal consumedAmount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        final BigDecimal availableBalance = amount.subtract(consumedAmount);
        return availableBalance.compareTo(BigDecimal.ZERO) > 0 ? availableBalance : BigDecimal.ZERO;
    }

    /**
     * 修改预付款记录，并返回是否给定的预付款记录支付了全部需要支付的费用
     *
     * @param prepayment
     * @param amount      结算金额
     * @param settledDate 结算时间
     * @return
     */
    public static boolean pay(Prepayment prepayment, BigDecimal amount, Date settledDate) {
        if (amount == null) {
            return true;
        }
        if (amount.compareTo(prepayment.amount) > 0) {
            prepayment.withdrawAmount = prepayment.amount;
            prepayment.settlementStatus = SettlementStatus.CLEARED;
            prepayment.save();
            return false;
        } else {
            prepayment.withdrawAmount = prepayment.withdrawAmount == null ? amount : prepayment.withdrawAmount.add(amount);
            if (prepayment.withdrawAmount.compareTo(prepayment.amount) == 0 || (settledDate != null && settledDate.after(prepayment.expireAt))) {
                prepayment.settlementStatus = SettlementStatus.CLEARED;
            }
            prepayment.save();
            return true;
        }
    }

    public static BigDecimal findAmountBySupplier(Supplier supplier) {
        BigDecimal amount = find("select sum(amount-withdrawAmount) from Prepayment where supplier=? and " +
                "amount>withdrawAmount and settlementStatus=? and deleted = ?", supplier,
                SettlementStatus.UNCLEARED, DeletedStatus.UN_DELETED).first();
        return amount == null ? BigDecimal.ZERO : amount;
    }

    /**
     * 获取商户的有效的预付款记录.
     *
     * @param supplier
     * @return
     */
    public static List<Prepayment> findBySupplier(Supplier supplier) {
        return find("supplier=? and amount>withdrawAmount and settlementStatus=? and deleted = ?",
                supplier, SettlementStatus.UNCLEARED, DeletedStatus.UN_DELETED).fetch();
    }

    /**
     * 获取指定商户的未过期的所有预付款记录.
     *
     * @param supplier
     * @return
     */
    public static List<Prepayment> findNotExpiredBySupplier(Supplier supplier) {
        return Prepayment.find("deleted = ? and expireAt>? and supplier.id=? and warning=false",
                DeletedStatus.UN_DELETED,
                new Date(),
                supplier.id).fetch();
    }

    public static void toHistoryData(Long id, String loginName) {
        Prepayment prepayment = Prepayment.findById(id);
        PrepaymentHistory history = new PrepaymentHistory();
        history.amount = prepayment.amount;
        history.createdAt = new Date();
        history.createdBy = loginName;
        history.prepaymentId = id;
        history.effectiveAt = prepayment.effectiveAt;
        history.expireAt = prepayment.expireAt;
        history.remark = prepayment.remark;
        history.supplier = prepayment.supplier;
        history.withdrawAmount = prepayment.withdrawAmount;
        history.settlementStatus = prepayment.settlementStatus;
        history.save();

    }

    public BigDecimal getSalesAmountUntilNow() {
        final Date endOfDay = DateUtil.getEndOfDay(new Date());
        BigDecimal amount = BigDecimal.ZERO;   //可结算总额
        //只能处理视惠的结算.
        Account supplierAccount = AccountUtil.getSupplierAccount(this.supplier.id, Operator.defaultOperator());
        if (this.expireAt == null || this.effectiveAt == null) {
            return BigDecimal.ZERO;
        }

        if (endOfDay.compareTo(this.expireAt) < 0) {
            amount = supplierAccount.getWithdrawAmount(endOfDay);
        } else {
            amount = supplierAccount.getWithdrawAmount(expireAt);
        }
        return amount;
    }

    /*
        取得最大的可以结算的金额
     */
    public BigDecimal getMaxCanSettleSalesAmount() {
        return BigDecimal.ZERO;

    }

    /**
     * 预付款结算
     */
    public static void confirmSettle(Prepayment prepayment, String updatedBy) {
        Date toDate = DateUtils.truncate(new Date(), Calendar.DATE);
        Supplier supplier = Supplier.findById(prepayment.supplier.id);
        Account supplierAccount = Account.find("uid = ? and accountType = ?", supplier.id, AccountType.SUPPLIER).first();
        System.out.println("&&&&&&&&&supplierAccount.id = " +supplierAccount.id);
        BigDecimal tempClearedAmount = BigDecimal.ZERO;
        List<ClearedAccount> clearedAccountList = ClearedAccount.find(
                "accountId=? and settlementStatus=? and date < ? order by date",
                supplierAccount.id, SettlementStatus.UNCLEARED, toDate).fetch();
        for (ClearedAccount clearedAccount : clearedAccountList) {
            //记录每次clearedAccount中金额的累加值
            tempClearedAmount = tempClearedAmount.add(clearedAccount.amount);
            clearedAccount.settlementStatus = SettlementStatus.CLEARED;
            clearedAccount.prepayment = prepayment;
            clearedAccount.updatedBy = updatedBy;
            clearedAccount.updatedAt = new Date();
            clearedAccount.save();
            //如果累计结算金额超过预付款金额，则创建一条clearedAccount,记录两者差额
            if (tempClearedAmount.compareTo(prepayment.amount) > 0) {
                ClearedAccount addClearedAccount = new ClearedAccount();
                addClearedAccount.settlementStatus = SettlementStatus.UNCLEARED;
                addClearedAccount.accountId = supplierAccount.id;
                addClearedAccount.amount = tempClearedAmount.subtract(prepayment.amount);
                addClearedAccount.prepayment = prepayment;
                addClearedAccount.date = clearedAccount.date;
                addClearedAccount.save();
                break;
            }
            if (tempClearedAmount.compareTo(prepayment.amount) == 0) {
                break;
            }

        }

        //创建相应的1条TradeBill和2条AccountSequence
        TradeBill balancedTradeBill = TradeUtil.balanceBill(supplierAccount,
                AccountUtil.getPlatformWithdrawAccount(supplierAccount.operator), TradeType.PREPAYMENT_SETTLED,
                prepayment.amount, null);
        balancedTradeBill.save();
        TradeUtil.success(balancedTradeBill, "预付款结算", null, updatedBy);
        List<AccountSequence> sequences = AccountSequence.find("tradeId = ? and tradeType = ?", balancedTradeBill.id,
                TradeType.PREPAYMENT_SETTLED).fetch();
        for (AccountSequence sequence : sequences) {
            sequence.settlementStatus = SettlementStatus.CLEARED;
            sequence.save();
        }

        Prepayment.toHistoryData(prepayment.id, updatedBy);
    }
}
