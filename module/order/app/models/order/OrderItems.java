package models.order;

import models.sales.Goods;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "order_items")
public class OrderItems extends Model {
    @ManyToOne
    Orders order;

    @ManyToOne
    Goods goods;

    @Column(name="originalPrice")
    Float originalPrice;

    @Column(name="salePrice")
    Float salePrice;

    @Column(name="goods_name")
    String goodsName;

    Long number;

    @Column(name="createdAt")
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
