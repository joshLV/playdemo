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

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="goods_id",nullable=true)
	Goods goods;

	@Column(name="original_price")
	BigDecimal originalPrice;

	@Column(name="sale_price")
	BigDecimal salePrice;

	@Column(name="goods_name")
	String goodsName;
	
	@Column(name="buy_number")
	Long buyNumber;

	@Column(name="created_at")
	public Date  createdAt;

	public OrderItems(Orders order, Goods goods, long number){
		this.order = order;
		this.goods = goods;
		this.originalPrice = goods.originalPrice;
		this.salePrice = goods.salePrice;
		this.goodsName = goods.name;
		this.buyNumber = number;
		this.createdAt = new Date();
	}

}
