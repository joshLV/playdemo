package models.order;

import com.uhuila.common.constants.DeletedStatus;
import models.accounts.AccountSequence;
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

    @Column(name = "effective_at")
    public Date effectiveAt;    //有效期开始时间

    @Column(name = "expire_at")
    public Date expireAt;       //有效期结束时间

    public String remark;

    @Column(name = "created_at")
    public Date createdAt;      //创建时间

    @Column(name = "created_by")
    public String createdBy;    //创建人帐号

    public Date updatedAt;      //最后修改时间

    public String updatedBy;    //最后修改人帐号

    public DeletedStatus deleted;   //删除状态

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
    public boolean canUpdate() {
        return AccountSequence.countByPrepayment(id) <= 0l;
    }
}
