package models.supplier;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

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

    @Column(name = "effective_at")
    public Date effectiveAt;

    @Column(name = "expire_at")
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


    public SupplierContract(Supplier supplier) {
        this.supplierId = supplier.id;
        this.supplierName = supplier.otherName;
        this.supplierCompanyName = supplier.fullName;
        this.createdAt = new Date();
    }

}
