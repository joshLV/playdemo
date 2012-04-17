package models.order;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;

import models.sales.Goods;
import play.db.jpa.JPA;
import play.db.jpa.Model;

@Entity
@Table(name = "order_items")
public class OrderItems extends Model {

	// ====  价格列表  ====
	@Column(name = "face_value")
	public BigDecimal faceValue;		//商品面值、市场价

	@Column(name = "original_price")
	public BigDecimal originalPrice;	//供应商进货价

	@Column(name = "resaler_price")
	public BigDecimal resalerPrice;     //用户在哪个分销商平台购买的价格，用于计算分销平台的佣金

	@Column(name = "sale_price")
	public BigDecimal salePrice;		//最终成交价,对于普通分销商来说，此成交价与以上分销商价(resalerPrice)相同；
	// ====  价格列表  ====

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = true)
	public Order order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "goods_id", nullable = true)
	public Goods goods;

	@Column(name = "goods_name")
	public String goodsName;

	@Column(name = "buy_number")
	public Long buyNumber;

	public String phone;

	@Column(name = "created_at")
	public Date createdAt;

	public OrderItems(Order order, Goods goods, long buyNumber, String phone, BigDecimal salePrice, BigDecimal resalerPrice) {
		this.order = order;
		this.goods = goods;
		this.faceValue = goods.faceValue;
		this.originalPrice = goods.originalPrice;
		this.salePrice = salePrice;
		this.resalerPrice = resalerPrice;
		this.goodsName = goods.name;
		this.buyNumber = buyNumber;
		this.phone = phone;
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
		Object result = q.getSingleResult();
		return result == null ? 0 : (Long) result;
	}

	/**
	 * 处理券号
	 * @return 券号
	 */
	public String getEcouponSn(){
		Query query = play.db.jpa.JPA.em().createQuery(
				"select e from ECoupon e where e.order = :order and e.goods =:goods ");
		query.setParameter("order", this.order );
		query.setParameter("goods", this.goods );
		List<ECoupon> favs = query.getResultList();
		StringBuilder sn = new StringBuilder();
		for (ECoupon e:favs) {
			String eCouponSn = e.eCouponSn;
			int len =  eCouponSn.length();
			for (int i =0; i < len-4;i++) {
				sn.append("*");
			}
			sn.append(eCouponSn.substring(len-4,len)+"\n");
		}
		return sn.toString();
	}

	/**
	 * 处理券号
	 * @return 券号
	 */
	public String getWebEcouponSn(){
		Query query = play.db.jpa.JPA.em().createQuery(
				"select e from ECoupon e where e.order = :order and e.goods =:goods ");
		query.setParameter("order", this.order );
		query.setParameter("goods", this.goods );
		List<ECoupon> favs = query.getResultList();
		StringBuilder sn = new StringBuilder();
		for (ECoupon e:favs) {
			sn.append(e.eCouponSn);
			sn.append("\n");
		}
		return sn.toString();
	}
}
