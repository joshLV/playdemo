package models.order;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import models.sales.Goods;
import play.db.jpa.Model;

@Entity
@Table(name = "order_items")
public class OrderItems extends Model {
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="order_id",nullable=true)
	Orders order;

	@ManyToOne
	Goods goods;

	@Column(name="originalPrice")
	BigDecimal originalPrice;

	@Column(name="salePrice")
	BigDecimal salePrice;

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
