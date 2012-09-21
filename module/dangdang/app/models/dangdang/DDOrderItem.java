package models.dangdang;

import models.order.OrderItems;
import models.sales.Goods;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-15
 * Time: 下午1:35
 */
@Entity
@Table(name = "dd_order_item")
public class DDOrderItem extends Model {

    @Column(name = "dd_order_id", nullable = true)
    public Long ddOrderId;

    @Column(name = "dd_goods_id")
    public String ddGoodsId;  //当当团购编号，即当当的商品id

    public Long goodsId;  //来源网站团购编号,即一百券的商品id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ybq_order_items_id", nullable = true)
    public OrderItems ybqOrderItems;
    @Column(name = "created_at")
    public Date createdAt;

    public DDOrderItem(Long orderId, String ddgid, Goods goods, OrderItems ybqOrderItem) {
        this.ddOrderId = orderId;
        this.ddGoodsId = ddgid;
        this.goodsId = goods.id;
        this.createdAt = new Date();
        this.ybqOrderItems = ybqOrderItem;
    }

    public DDOrderItem() {
    }

    public static DDOrderItem findByOrder(OrderItems orderItems) {
        return find("byYbqOrderItems", orderItems).first();
    }
}
