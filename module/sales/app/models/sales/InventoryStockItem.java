package models.sales;

import play.data.validation.InFuture;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 库存变动明细.
 * <p/>
 * User: sujie
 * Date: 3/4/13
 * Time: 3:56 PM
 */
@Entity
@Table(name = "inventory_stock_item")
public class InventoryStockItem extends Model {

    @ManyToOne
    public Sku sku;

    /**
     * 出入库数量.
     * 入库为正，出库为负
     */
    @Required
    @Column(name = "change_count")
    public Long changeCount;

    /**
     * 剩余数量.
     */
    @Column(name = "remain_count")
    public Long remainCount;

    /**
     * 价格
     * 入库时表示采购价
     * 出库时表示售价
     */
    @Required
    @Column(name = "price")
    public BigDecimal price;

    /**
     * 券有效开始日
     */
    @Required
    @Column(name = "effective_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date effectiveAt;
    /**
     * 券有效结束日
     */
    @Required
    @InFuture
    @Column(name = "expire_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date expireAt;
}
