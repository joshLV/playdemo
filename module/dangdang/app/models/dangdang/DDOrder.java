/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package models.dangdang;

import models.order.NotEnoughInventoryException;
import models.sales.Goods;
import org.dom4j.Element;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
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

    @Column(name = "order_create_time")
    public Date orderCreateTime;        //订单创建日期

    @Column(name = "express_fee")
    public BigDecimal expressFee; //运费


    @Column(name = "update_time")
    public Date updateTime;             //更新时间

    @Column(name = "created_at")
    public Date createdAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    public List<DDOrderItem> orderItems;
    @Column(name = "user_id")
    public String userCode;
    /**
     * 当当团购编号
     */
    @Transient
    public Long ddgid;
    @Transient
    public Long spgid;//来源网站团购编号


    @Column(name = "consume_id")
    public String consumeId;//消费权唯一的标志
    @Column(name = "receive_mobile_tel")
    public String receiveMobile;//团购顾客手机


    public DDOrder(Long orderId, BigDecimal orderAmount, BigDecimal amount, Long userId) {
        this.orderAmount = orderAmount;
        this.amount = amount;
        this.orderId = orderId;
        this.userCode = String.valueOf(userId);
        this.createdAt = new Date();
    }


    public DDOrderItem addOrderItem(Goods goods, Integer number, String mobile, BigDecimal salePrice)
            throws NotEnoughInventoryException {
        DDOrderItem orderItem = null;
        if (number > 0 && goods != null) {
            checkInventory(goods, number);
            orderItem = new DDOrderItem(this, goods, number, mobile, salePrice);
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

    /**
     * 处理当当过来的订单.
     */
    public void handleOrder() {
        createAndUpdateInventory();
        payAndSendECoupon();
    }

    public void createAndUpdateInventory() {
    }

    public void payAndSendECoupon() {

    }


    // 订单摘要解析器
    public static Parser<DDOrder> parser = new Parser<DDOrder>() {
        @Override
        public DDOrder parse(Element node) {
            DDOrder order = new DDOrder();
            order.orderId = Long.parseLong(node.elementTextTrim("order_id"));
            order.ddgid = Long.parseLong(node.elementTextTrim("ddgid"));
            order.spgid = Long.parseLong(node.elementTextTrim("spgid"));
            order.userCode = node.elementTextTrim("user_code");
            order.receiveMobile = node.elementTextTrim("receiveMobile");

            order.consumeId = node.elementTextTrim("consumeId");

            return order;
        }
    };

}