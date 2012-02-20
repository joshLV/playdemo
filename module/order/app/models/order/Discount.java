package models.order;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name = "discount")
public class Discount extends Model {

	@ManyToOne
	public Orders orders;

	@Column(name="discount_sn")
	public String discountSn;

	@Column(name="order_id")
	public Long orderId;

	@Column(name="discount_price")
	public Float discountPrice;

	@Column(name="refund_price")
	public Float refundPrice;

	@Column(name="created_at")
	public Date createdAt;
	@Column(name="refund_at")
	public Date refundAt;
	public String status;
}
