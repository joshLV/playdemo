package models.order;

import models.sales.Goods;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "order_items")
public class OrderItems extends Model {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    public Order order;

    @OneToOne(mappedBy = "orderItems")
    public ECoupon eCoupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = true)
    public Goods goods;

    @Column(name = "original_price")
    public BigDecimal originalPrice;

    @Column(name = "sale_price")
    public BigDecimal salePrice;

    @Column(name = "goods_name")
    public String goodsName;

    @Column(name = "buy_number")
    public Long buyNumber;

    @Column(name = "created_at")
    public Date createdAt;

    public OrderItems(Order order, Goods goods, long buyNumber) {
        this.order = order;
        this.goods = goods;
        this.originalPrice = goods.originalPrice;
        this.salePrice = goods.salePrice;
        this.goodsName = goods.name;
        this.buyNumber = buyNumber;
        this.createdAt = new Date();
    }


    public static long itemsNumber(Order order) {
        long itemsNumber = 0L;
        if (order == null) {
            return itemsNumber;
        }
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT sum( buyNumber ) FROM OrderItems WHERE order = :order");
        q.setParameter("order", order);
        return (Long) q.getSingleResult();
    }


}
