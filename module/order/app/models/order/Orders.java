package models.order;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import models.consumer.User;
import play.db.jpa.Model;

@Entity
@Table(name = "orders")
public class Orders extends Model {
	@ManyToOne
	public User user;

	@Column(name="order_no")
	public String orderNumber;

	public String status;

	public float amount;

	@Column(name="account_pay")
	public float accountPay;

	@Column(name="discount_pay")
	public float discountPay;

	@Column(name="need_pay")
	public float needPay;

	@Column(name="buyer_phone")
	public String buyerPhone;

	@Column(name="buyer_mobile")
	public String buyerMobile;

	public String remark;

	@Column(name="pay_method")
	public String payMethod;

	@Column(name="pay_request_id")
	public Long payRequestId;

	@Column(name="receiver_phone")
	public String receiverPhone;

	@Column(name="receiver_mobile")
	public String receivMobile;

	@Column(name="receiver_address")
	public String receivAddress;

	@Column(name="receiver_name")
	public String receivName;

	public String postcode;

	@Column(name="createdAt")
	public Date createdAt;

	@Column(name="updatedAt")
	public Date updatedAt;

	@Column(name="lock_version")
	public int lockVersion;

	public int deleted;

	@Column(name="delivery_no")
	public String deliveryNo;
	@Column(name="delivery_type")
	public int deliveryType;
	/** 成交开始时间*/
	@Transient
	public String createdAtBegin;
	/** 成交开始时间*/
	@Transient
	public String createdAtEnd;
	/**退款开始时间*/
	@Transient
	public String refundAtBegin;
	/** 退款开始时间*/
	@Transient
	public String refundAtEnd;

	public Orders(User user){
		this.user = user;
		this.status = OrderStatus.UNPAID.toString();
		this.deleted = 0;
		this.orderNumber = "";


		this.amount         = 0;
		this.accountPay     = 0;
		this.needPay        = 0;
		this.discountPay    = 0;

		this.lockVersion    = 0;

		this.createdAt = new Date();
		this.updatedAt = new Date();
	}

	/**
	 * 订单查询 
	 * @param orders
	 * @return
	 */
	public static List query(Orders orders) {
		EntityManager entityManager = play.db.jpa.JPA.em();
		StringBuffer sql= new StringBuffer();
		sql.append("SELECT c.no,a.delivery_type,a.order_no,a.delivery_no,c.name,b.number,a.amount,a.created_at,a.pay_method,a.status  FROM orders a");
		sql.append(" LEFT JOIN order_items b ON a.id = b.order_id ");
		sql.append(" LEFT JOIN goods c ON b.goods_id=c.id");
		//指定某商户
		sql.append(" WHERE c.company_id=1");
		if(orders.createdAtBegin !=null) {
			sql.append(" and a.created_at >='"+orders.createdAtBegin+" 00:00:00'");
		}
		if(orders.createdAtEnd !=null) {
			sql.append(" and a.created_at <='"+orders.createdAtEnd+" 23:59:59'");
		}
		//		if(orders.refundAtBegin !=null) {
		//			sql.append(" and a.created_at >='"+orders.refundAtBegin+" 00:00:00'");
		//		}
		//		if(orders.refundAtEnd !=null) {
		//			sql.append(" and a.created_at <='"+orders.refundAtEnd+" 23:59:59'");
		//		}
		if(orders.status !=null && !"".equals(orders.status)) {
			sql.append(" and a.status ="+orders.status);
		}
		if(orders.deliveryType !=0) {
			sql.append(" and a.delivery_type ="+orders.deliveryType);
		}
		if(orders.payMethod !=null && !"".equals(orders.payMethod)) {
			sql.append(" and a.pay_method ="+orders.payMethod);
		}

		sql.append(" ORDER BY a.created_at DESC");
		List orderList = entityManager.createNativeQuery(sql.toString()).getResultList();
		return orderList;
	}

	public static List queryQ() {

		EntityManager entityManager = play.db.jpa.JPA.em();
		StringBuffer sql= new StringBuffer();
		sql.append("SELECT a.order_no,c.no,c.name,d.discount_sn,d.discount_price,a.created_at,d.refund_price,d.refund_at,d.status FROM discount d");
		sql.append(" LEFT JOIN orders a ON a.id = d.order_id ");
		sql.append(" LEFT JOIN order_items b ON a.id = b.order_id ");
		sql.append(" LEFT JOIN goods c ON b.goods_id=c.id");
		sql.append(" WHERE c.company_id=1");
		sql.append(" ORDER BY a.created_at DESC");
		List quanList = entityManager.createNativeQuery(sql.toString()).getResultList();
		return quanList;
	}

}
