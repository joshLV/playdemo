package models.order;

import models.sales.Sku;
import play.data.validation.InFuture;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 采购合同明细
 * <p/>
 * User: wangjia
 * Date: 13-3-29
 * Time: 下午2:57
 */
@Entity
@Table(name = "purchase_item")
public class PurchaseItem extends Model {
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    public PurchaseOrder purchaseOrder;

    @ManyToOne
    public Sku sku;

    /*
       采购数量
     */
    public Long count;

    /*
        进价
     */
    public BigDecimal price;

    /**
     * 有效开始日
     */
    @Column(name = "effective_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date effectiveAt;

    /**
     * 有效结束日
     */
    @InFuture
    @Column(name = "expire_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date expireAt;

    /**
     * 删除状态
     */
    @Enumerated(EnumType.ORDINAL)
    public com.uhuila.common.constants.DeletedStatus deleted;


}
