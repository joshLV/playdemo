package models.supplier;

import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

/**
 * 商户的商品分类.
 *
 * User: wangjia
 * Date: 12-11-29
 * Time: 下午2:00
 */
@Entity
@Table(name = "supplier_categories")
public class SupplierCategory extends Model {

    /**
     * 编码
     */
    @Unique
    @Required
    @Column(name = "code")
    public String code;

    /**
     * 类别名称
     */
    @Required
    @Column(name = "name")
    public String name;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    public Date createdAt;

    /**
     * 修改时间
     */
    @Column(name = "updated_at")
    public Date updatedAt;

    /**
     * 创建人
     */
    @Column(name = "created_by")
    public String createdBy;
    /**
     * 修改人
     */
    @Column(name = "updated_by")
    public String updatedBy;

    @Override
    public boolean create() {
        createdAt = new Date();
        return super.create();
    }

    public static void update(Long id, SupplierCategory supplierCategory) {
        SupplierCategory sc = findById(id);
        if (sc == null) {
            return;
        }
        sc.name = supplierCategory.name;
        sc.updatedBy = supplierCategory.updatedBy;
        sc.updatedAt = new Date();
        sc.save();
    }

    public static JPAExtPaginator<SupplierCategory> findByCondition(SupplierCategoryCondition condition,
                                                                    int pageNumber, int pageSize) {

        JPAExtPaginator<SupplierCategory> supplierCategoryPage = new JPAExtPaginator<>
                ("SupplierCategory sc", "sc", SupplierCategory.class, condition.getFilter(),
                        condition.getParamMap())
                .orderBy(condition.getOrderByExpress());
        supplierCategoryPage.setPageNumber(pageNumber);
        supplierCategoryPage.setPageSize(pageSize);
        supplierCategoryPage.setBoundaryControlsEnabled(false);
        return supplierCategoryPage;
    }

    public List<Supplier> getSuppliers() {
        return Supplier.find("supplierCategory.id=?", this.id
        ).fetch();
    }
}
