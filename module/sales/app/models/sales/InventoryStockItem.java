package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import models.order.PurchaseItem;
import models.order.Vendor;
import play.data.validation.InFuture;
import play.data.validation.Match;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;
import play.modules.view_ext.annotation.Money;

import javax.persistence.*;
import java.beans.Transient;
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


    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_stock_id")
    public InventoryStock stock;

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    public PurchaseItem purchaseItem;

    @ManyToOne
    public Sku sku;


    /**
     * 出入库数量.
     * 入库为正，出库为负
     */
    @Required
    @Min(0)
    @Match(value = "^[0-9]*$", message = "数量格式不对!(纯数字)")
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
    @Min(0)
    @Money
    @Column(name = "price")
    public BigDecimal price;

    /**
     * 创建时间
     * 出入库时间
     */
    @Column(name = "created_at")
    public Date createdAt;


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
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    public InventoryStockItem(InventoryStock stock) {
        this.stock = stock;
        this.createdAt = new Date();
        this.deleted = DeletedStatus.UN_DELETED;
    }

    /**
     * 库存明细查询
     *
     * @param condition
     * @param pageNumber
     * @param pageSize
     * @return
     */
    public static JPAExtPaginator<InventoryStockItem> findByCondition(InventoryStockItemCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<InventoryStockItem> stockItemPage = new JPAExtPaginator<>("InventoryStockItem i", "i", InventoryStockItem.class, condition.getFilter(), condition.getParamMap()).orderBy("i.createdAt desc");
        stockItemPage.setPageNumber(pageNumber);
        stockItemPage.setPageSize(pageSize);
        stockItemPage.setBoundaryControlsEnabled(false);
        return stockItemPage;
    }

    @Transient
    public String getChangeCountSign() {
        if (this.changeCount > 0) {
            return "+";
        } else {
            return "-";
        }
    }

    @Transient
    public Long getChangeCountAbsoluteValue() {
        return Math.abs(this.changeCount);
    }


}
