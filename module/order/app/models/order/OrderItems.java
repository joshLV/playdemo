package models.order;

import models.sales.Goods;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "order_items")
public class OrderItems extends Model {
    @ManyToOne
    Orders order;

    @ManyToMany
    Goods goods;

    @Column(name="original_price")
    Float originalPrice;

    @Column(name="sale_price")
    Float salePrice;

    @Column(name="goods_name")
    String goodsName;

    Long number;

    @Column(name="created_at")
    public Date  createdAt;

    public OrderItems(Orders order, Goods goods, long number){
        this.order = order;
        this.goods = goods;
        this.originalPrice = goods.originalPrice;
        this.salePrice = goods.salePrice;
        this.goodsName = goods.name;
        this.number = number;
        this.createdAt = new Date();
    }

}
