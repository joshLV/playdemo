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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dd_order_id", nullable = true)
    public DDOrder order;

    public Long ddgid;  //当当团购编号，即当当的商品id

    public Long spgid;  //来源网站团购编号,即一百券的商品id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ybq_order_items_id", nullable = true)
    public OrderItems ybqOrderItems;

    @Column(name = "goods_name")
    public String goodsName; //商品名称

    @Column(name = "order_item_num")
    public Integer orderItemNum;

    @Column(name = "order_item_price")
    public BigDecimal orderItemPrice;

    @Column(name = "original_price")
    public BigDecimal originalPrice;
    @Column(name = "phone")
    public String phone;

    @Column(name = "created_at")
    public Date createdAt;

    public DDOrderItem(DDOrder order, Goods goods, Integer buyNumber, String phone, BigDecimal salePrice, OrderItems ybqOrderItem) {
        this.order = order;
        this.goodsName = goods.name;
        this.originalPrice = goods.originalPrice;
        this.orderItemPrice = salePrice;
        this.goodsName = goods.name;
        this.orderItemNum = buyNumber;
        this.phone = phone;
        this.createdAt = new Date();
        this.ybqOrderItems = ybqOrderItem;
    }

    public DDOrderItem() {
    }

    /**
     * 当前订单项总费用：
     * lineValue = orderItemPrice*orderItemNum
     *
     * @return 订单项总费用
     */
    @Transient
    public BigDecimal getLineValue() {
        return orderItemPrice.multiply(new BigDecimal(orderItemNum));
    }

    public static DDOrderItem findByOrder(OrderItems orderItems) {
        return find("byYbpOrderItems", orderItems).first();
    }
}
