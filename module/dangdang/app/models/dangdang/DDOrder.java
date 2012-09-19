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
@Table(name = "dangdang_order")
public class DDOrder extends Model {
    @Column(name = "kx_order_id", unique = true)
    public Long orderId;                // 当当订单编号
    @Column(name = "order_amount")
    public BigDecimal orderAmount;      //订购金额（实际支付金额，包括运费）

    @Column(name = "amount")
    public BigDecimal amount;    //总额(单价*数量)

    @Column(name = "express_fee")
    public BigDecimal expressFee; //运费

    @Column(name = "status")
    public DDOrderStatus status;

    @Column(name = "created_at")
    public Date createdAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    public List<DDOrderItem> orderItems;
    @Column(name = "user_id")
    public String userCode;

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
        this.userCode = String.valueOf(userId);
        this.status = DDOrderStatus.ORDER_ACCEPT;
        this.createdAt = new Date();
    }


    public DDOrderItem addOrderItem(Goods goods, Integer number, String mobile, BigDecimal salePrice, OrderItems ybqOrderItem)
            throws NotEnoughInventoryException {
        DDOrderItem orderItem = null;
        if (number > 0 && goods != null) {
            checkInventory(goods, number);
            orderItem = new DDOrderItem(this, goods, number, mobile, salePrice, ybqOrderItem);
            //通过推荐购买的情况
            this.orderItems.add(orderItem);
            this.amount = this.amount.add(orderItem.getLineValue()); //计算折扣价
        }
        return orderItem;
    }

    public void checkInventory(Goods goods, long number) throws NotEnoughInventoryException {
        if (goods.baseSale < number) {
            throw new NotEnoughInventoryException();
        }
    }

    public DDOrder() {
    }


    public void createAndUpdateInventory() {
        //处理完毕
        this.status = DDOrderStatus.ORDER_FINISH;
        save();
        for (DDOrderItem orderItem : orderItems) {
            orderItem.save();
        }
    }


    public static DDOrder findByOrder(Order order) {
        return find("byYbqOrder", order).first();
    }
}