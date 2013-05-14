package models.order;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单项成本费用记录表.
 * 这里记录的费用都是不走TradeBill记录的，用于成本核算。
 */
@Entity
@Table(name="order_items_fees")
public class OrderItemsFee extends Model {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = true)
    public OrderItems orderItems;

    @Enumerated(EnumType.STRING)
    @Column(name="fee_type")
    public OrderItemsFeeType feeType;

    @Column
    public BigDecimal fee;

    @Column(name = "created_at")
    public Date createdAt;

    public static void recordFee(OrderItems orderItems, OrderItemsFeeType feeType, BigDecimal fee) {
        OrderItemsFee orderItemsFee = new OrderItemsFee();
        orderItemsFee.orderItems = orderItems;
        orderItemsFee.feeType = feeType;
        orderItemsFee.fee = fee;
        orderItemsFee.createdAt = new Date();
        orderItemsFee.save();
    }

    public static void recordFee(Long orderItemsId, OrderItemsFeeType feeType, BigDecimal fee) {
         OrderItems oi = OrderItems.findById(orderItemsId);
        if (oi != null) {
            recordFee(oi, feeType, fee);
        }
    }
}
