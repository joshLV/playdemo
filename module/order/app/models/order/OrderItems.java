package models.order;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import models.sales.Goods;
import play.db.jpa.Model;

@Entity
@Table(name = "order_items")
public class OrderItems extends Model {
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="order_id",nullable=true)
	public Orders order;

	@OneToOne(mappedBy = "orderItems")
	public ECoupon eCoupon;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="goods_id",nullable=true)
	public Goods goods;

	@Column(name="original_price")
	public BigDecimal originalPrice;

	@Column(name="sale_price")
	public BigDecimal salePrice;

	@Column(name="goods_name")
	public String goodsName;
	
	@Column(name="buy_number")
	public Long buyNumber;

	@Column(name="created_at")
	public Date  createdAt;

	public OrderItems(Orders order, Goods goods, long buyNumber){
		this.order = order;
		this.goods = goods;
		this.originalPrice = goods.originalPrice;
		this.salePrice = goods.salePrice;
		this.goodsName = goods.name;
		this.buyNumber = buyNumber;
		this.createdAt = new Date();
	}

}
