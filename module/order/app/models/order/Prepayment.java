package models.order;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import models.accounts.AccountSequence;
import models.accounts.SettlementStatus;
import models.supplier.Supplier;
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
        return AccountSequence.countByPrepayment(id) <= 0l;
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

}
