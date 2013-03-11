package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import play.data.validation.InFuture;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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


    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_stock_id")
    public InventoryStock inventoryStock;

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
     * 创建时间
     * 出入库时间
     */
    @Column(name = "created_at")
    public Date createdAt;


    /**
     * 券有效开始日
     */
    @Column(name = "effective_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date effectiveAt;

    /**
     * 券有效结束日
     */
    @InFuture
    @Column(name = "expire_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date expireAt;


    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    public InventoryStockItem(InventoryStock stock) {
        this.inventoryStock = stock;
        this.sku = stock.sku;

        if (stock.actionType == StockActionType.IN) {
            this.changeCount = stock.stockInCount;
            this.remainCount = stock.stockInCount;
            this.price = stock.originalPrice;
        } else {
            this.changeCount = stock.stockOutCount;
            this.price = stock.salePrice;
        }
        this.effectiveAt = stock.effectiveAt;
        this.expireAt = stock.expireAt;
        this.createdAt = new Date();
        this.deleted = DeletedStatus.UN_DELETED;
    }



}
