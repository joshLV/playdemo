package models.supplier;

import org.apache.commons.lang.StringUtils;
import play.data.validation.InFuture;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import util.common.InfoUtil;

/**
 * 商户合同
 * <p/>
 * User: wangjia
 * Date: 13-1-25
 * Time: 上午10:27
 */
@Entity
@Table(name = "supplier_contract")
public class SupplierContract extends Model {


    @Column(name = "supplier_name")
    public String supplierName;

    @Column(name = "supplier_company_name")
    public String supplierCompanyName;

    @Required
    @Column(name = "effective_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date effectiveAt;

    @Required
    @Column(name = "expire_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date expireAt;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "updated_at")
    public Date updatedAt;

    @Column(name = "updated_by")
    public String updatedBy;

    public String description;

    @OneToMany(mappedBy = "contract")
    public List<SupplierContractImage> supplierContractImagesList;

    /**
     * 所属商户ID
     */
    @Column(name = "supplier_id")
    public Long supplierId;

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public com.uhuila.common.constants.DeletedStatus deleted;

    /**
     * 得到隐藏处理过的券号
     *
     * @return 券号
     */
    @Transient
    public String getSpecifiedLengthDescription() {
        return util.common.InfoUtil.getSpecifiedLengthDescription(10, description);
    }

    public SupplierContract(Supplier supplier) {
        this.supplierId = supplier.id;
        this.supplierName = supplier.otherName;
        this.supplierCompanyName = supplier.fullName;
        this.createdAt = new Date();
    }

    public static void update(Long id, SupplierContract contract) {
        SupplierContract sourceContract = SupplierContract.findById(id);
        sourceContract.effectiveAt = contract.effectiveAt;
        sourceContract.expireAt = contract.expireAt;
        sourceContract.description = contract.description;
        sourceContract.updatedAt = new Date();
        sourceContract.updatedBy = contract.updatedBy;
        sourceContract.save();
    }

    @Override
    public boolean create() {
        deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED;
        createdAt = new Date();
        return super.create();
    }

    public static void delete(long id) {
        SupplierContract contract = SupplierContract.findById(id);
        if (contract == null) {
            return;
        }
        if (!com.uhuila.common.constants.DeletedStatus.DELETED.equals(contract.deleted)) {
            contract.deleted = com.uhuila.common.constants.DeletedStatus.DELETED;
            contract.save();
        }
    }

    public static JPAExtPaginator<SupplierContract> findByCondition(SupplierContractCondition condition,
                                                                    int pageNumber, int pageSize) {

        JPAExtPaginator<SupplierContract> goodsPage = new JPAExtPaginator<>
                ("SupplierContract c", "c", SupplierContract.class, condition.getFilter(),
                        condition.getParamMap())
                .orderBy(condition.getOrderByExpress());
        goodsPage.setPageNumber(pageNumber);
        goodsPage.setPageSize(pageSize);
        goodsPage.setBoundaryControlsEnabled(false);
        return goodsPage;
    }

}
