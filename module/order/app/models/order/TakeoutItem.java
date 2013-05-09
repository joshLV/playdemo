package models.order;

import models.sales.Goods;
import models.sales.Sku;
import org.apache.commons.lang.builder.ToStringBuilder;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * 出库表
 * <p/>
 * User: sujie
 * Date: 3/12/13
 * Time: 2:11 PM
 */
@Table(name = "take_out_items")
@Entity
public class TakeoutItem extends Model {
    /**
     * 出库的orderItem.
     */
    @ManyToOne
    public OrderItems orderItem;

    @ManyToOne
    public Sku sku;

    public Long count;

    public TakeoutItem(Goods goods, Long count) {
        this.sku = goods.sku;
        this.count = count * goods.skuCount;
    }

    public TakeoutItem( Sku sku, Long count) {
        this.sku = sku;
        this.count = count;
    }
    public TakeoutItem(OrderItems orderItem, Sku sku, Long count) {
        this.orderItem = orderItem;
        this.sku = sku;
        this.count = count;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("orderItem", orderItem)
                .append("sku", sku)
                .append("count", count)
                .toString();
    }
}
