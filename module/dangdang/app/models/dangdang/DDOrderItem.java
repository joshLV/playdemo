package dangdang;

import models.sales.Goods;
import play.db.jpa.Model;

import javax.persistence.*;
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
    @Column(name = "order_item_id")
    public Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    public DDOrder order;

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

    public DDOrderItem(DDOrder order, Goods goods, Integer buyNumber, String phone, BigDecimal salePrice) {
        this.order = order;
        this.goodsName = goods.name;
        this.originalPrice = goods.originalPrice;
        this.orderItemPrice = salePrice;
        this.goodsName = goods.name;
        this.orderItemNum = buyNumber;
        this.phone = phone;
        this.createdAt = new Date();
    }

    /**
     * 当前订单项总费用：
     * lineValue = orderItemPrice*orderItemNum
     */
    @Transient
    public BigDecimal getLineValue() {
        return orderItemPrice.multiply(new BigDecimal(orderItemNum));
    }
}
