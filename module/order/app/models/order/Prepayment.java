package models.order;

import com.uhuila.common.constants.DeletedStatus;
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

    public DeletedStatus deleted;   //删除状态

    @Column(name = "settlement_status")
    public SettlementStatus settlementStatus;   //结算状态

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
        oldPrepayment.expireAt = prepayment.expireAt;
        oldPrepayment.remark = prepayment.remark;
        oldPrepayment.save();
    }

    public static ModelPaginator<Prepayment> getPage(int pageNumber, int pageSize, Long supplierId) {
        ModelPaginator<Prepayment> page;
        if (supplierId != null) {
            page = new ModelPaginator<>(Prepayment.class, "deleted = ? and supplier.id=?", DeletedStatus.UN_DELETED,
                    supplierId).orderBy("supplier,createdAt desc");
        } else {
            page = new ModelPaginator<>(Prepayment.class, "deleted = ? ", DeletedStatus.UN_DELETED).orderBy("supplier,createdAt desc");
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

    public static List<Prepayment> getUnclearedPrepayments(long uid) {
        return find("supplier.id=? and amount>withdrawAmount order by createdAt", uid).fetch();
    }

    public static BigDecimal getUnclearedBalance(Long uid) {
        BigDecimal prepaymentAmount = find("select sum(amount)-sum(withdrawAmount) from Prepayment where supplier.id=? and amount>withdrawAmount", uid).first();
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

    /**
     * 修改预付款记录，并返回是否给定的预付款记录支付了全部需要支付的费用
     *
     * @param prepayment
     * @param amount
     * @return
     */
    public static boolean pay(Prepayment prepayment, BigDecimal amount) {
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
            if (prepayment.withdrawAmount.compareTo(prepayment.amount) == 0) {
                prepayment.settlementStatus = SettlementStatus.CLEARED;
            }
            prepayment.save();
            return true;
        }
    }

    public static BigDecimal findAmountBySupplier(Supplier supplier) {
        BigDecimal amount = find("select sum(amount-withdrawAmount) from Prepayment where supplier=? and expireAt>? and amount>withdrawAmount", supplier, new Date()).first();
        return amount == null ? BigDecimal.ZERO : amount;
    }

    /**
     * 获取商户的有效的预付款记录.
     *
     * @param supplier
     * @return
     */
    public static List<Prepayment> findBySupplier(Supplier supplier) {
        return find("supplier=? and expireAt>? and amount>withdrawAmount and settlement=?", supplier, new Date(), SettlementStatus.UNCLEARED).fetch();
    }
}
