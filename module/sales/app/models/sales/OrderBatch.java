package models.sales;

import models.supplier.Supplier;
import play.db.jpa.Model;
import play.modules.paginate.ModelPaginator;

import javax.persistence.*;
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
     * 订单数
     */
    @Column(name = "order_count")
    public Long orderCount;
    /**
     * 创建人姓名
     */
    @Column(name = "created_by")
    public String createdBy;

    @Transient
    public Boolean changedInfo = Boolean.FALSE;

    /**
     * 对应的出库单.
     * 只有视惠自己发货的批次才需要填写这个字段
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = true)
    public InventoryStock stock;


    public OrderBatchStatus status = OrderBatchStatus.VALID;

    public OrderBatch() {
    }

    public OrderBatch(Supplier supplier, String createdBy, Long orderCount) {
        this.orderCount = orderCount;
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
