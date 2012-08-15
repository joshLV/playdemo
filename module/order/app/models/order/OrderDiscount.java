package models.order;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import play.db.jpa.Model;

@Entity
@Table(name="order_discounts")
public class OrderDiscount extends Model {

    private static final long serialVersionUID = 9821912330652L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_code_id")
    public DiscountCode discountCode;

    /**
     * 折扣金额. 正数.
     */
    @Column(name = "discount_amount")
    public BigDecimal discountAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    public Order order;

    /**
     * 如果在单个订单项目上折扣，这里记录订单项目.
     * 如果是整个订单上折扣，这个字段为空.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = true)
    public OrderItems orderItem;
    
    
}
