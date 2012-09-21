/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package models.dangdang;

import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "dd_order")
public class DDOrder extends Model {
    @Column(name = "dd_order_id", unique = true)
    public Long orderId;                // 当当订单编号
    @Column(name = "order_amount")
    public BigDecimal orderAmount;      //订购金额（实际支付金额，包括运费）

    @Column(name = "amount")
    public BigDecimal amount;    //总额(单价*数量)

    @Column(name = "express_fee")
    public BigDecimal expressFee; //运费

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    public DDOrderStatus status;

    @Column(name = "created_at")
    public Date createdAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    public List<DDOrderItem> orderItems;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ybq_order_id", nullable = true)
    public Order ybqOrder;

    @Column(name = "receive_mobile_tel")
    public String receiveMobile;//团购顾客手机


    public DDOrder(Long orderId, BigDecimal orderAmount, BigDecimal amount, BigDecimal expressFee, Long userId) {
        this.orderAmount = orderAmount;
        this.amount = amount;
        this.expressFee = expressFee;
        this.orderId = orderId;
        this.status = DDOrderStatus.ORDER_ACCEPT;
        this.createdAt = new Date();
    }


    public DDOrderItem addOrderItem(Goods goods, Long ddgid, Integer number, String mobile, BigDecimal salePrice, OrderItems ybqOrderItem) {
        DDOrderItem orderItem = null;
        if (number > 0 && goods != null) {
            orderItem = new DDOrderItem(this, ddgid, goods, number, mobile, salePrice, ybqOrderItem);
            //通过推荐购买的情况
            this.orderItems.add(orderItem);
            this.amount = this.amount.add(orderItem.getLineValue()); //计算折扣价
        }
        return orderItem;
    }

    public DDOrder() {
    }

    public static DDOrder findByOrder(Order order) {
        return find("byYbqOrder", order).first();
    }
}