package models.sales;

import models.supplier.Supplier;
import play.db.jpa.Model;
import play.modules.paginate.ModelPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * 订单发货批次.
 * <p/>
 * User: sujie
 * Date: 3/11/13
 * Time: 3:56 PM
 */
@Entity
@Table(name = "order_batch")
public class OrderBatch extends Model {
    /**
     * 商户.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = true)
    public Supplier supplier;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    public Date createdAt = new Date();

    /**
     * 创建人姓名
     */
    @Column(name = "created_by")
    public String createdBy;

    /**
     * 对应的出库单.
     * 只有视惠自己发货的批次才需要填写这个字段
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = true)
    public InventoryStock stock;

    public OrderBatch() {
    }

    public OrderBatch(Supplier supplier, String createdBy) {
        this.supplier = supplier;
        this.createdBy = createdBy;
    }

    /**
     * 获取指定商户的订单批次列表.
     */
    public static ModelPaginator<OrderBatch> findBySupplier(Long supplierId, int pageNumber, int pageSize) {
        ModelPaginator<OrderBatch> page = new ModelPaginator<>(OrderBatch.class, "supplier.id=? order by createdAt desc", supplierId);
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }
}
