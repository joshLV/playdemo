package models.ktv;

import com.uhuila.common.constants.DeletedStatus;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.ModelPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author likang
 *         Date: 13-5-6
 *         <p/>
 *         KTV 产品，一个产品下有多个价格排期(KtvPriceSchedule)
 */
@Entity
@Table(name = "ktv_products")
public class KtvProduct extends Model {
    @ManyToOne
    @JoinColumn(name = "supplier_id")
    public Supplier supplier;

    @Required
    public String name;//产品名称

    @Required
    public int duration;//欢唱时长

    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "updated_by")
    public String updatedBy;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

    public static void update(Long id, KtvProduct product, String updatedBy) {
        KtvProduct currentProduct = KtvProduct.findById(id);
        if (currentProduct == null) {
            return;
        }
        currentProduct.name = product.name;
        currentProduct.duration = product.duration;
        currentProduct.supplier = product.supplier;
        currentProduct.updatedAt = new Date();
        currentProduct.updatedBy = updatedBy;
        currentProduct.save();
    }

    public static ModelPaginator getProductPage(int pageNumber, int pageSize, Long supplierId, String name) {
        ModelPaginator page;
        StringBuilder sq = new StringBuilder();
        sq.append("deleted = ?");
        List<Object> list = new ArrayList<Object>();
        list.add(DeletedStatus.UN_DELETED);
        if (supplierId != null && supplierId.longValue() != 0) {
            sq.append("and supplier.id=?");
            list.add(supplierId);
        }
        if (StringUtils.isNotBlank(name)) {
            sq.append(" and name like ?");
            list.add("%" + name + "%");
        }
        page = new ModelPaginator(KtvProduct.class, sq.toString(), list.toArray()).orderBy("createdAt desc,name");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

    public static List<KtvProduct> findProductBySupplier(Long supplierId) {
        Supplier supplier = Supplier.findById(supplierId);
        return KtvProduct.find("bySupplierAndDeleted", supplier, DeletedStatus.UN_DELETED).fetch();
    }


}
