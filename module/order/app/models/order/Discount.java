package models.order;

import java.util.Date;

import javax.persistence.*;

import play.db.jpa.Model;

@Entity
@Table(name = "discount")
public class Discount extends Model {

	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="order_id",nullable=true)
    public Order order;
	@Column(name="discount_sn")
	public String discountSn;

	@Column(name="discount_price")
	public Float discountPrice;

	@Column(name="refund_price")
	public Float refundPrice;

	@Column(name="created_at")
	public Date createdAt;
	@Column(name="refund_at")
	public Date refundAt;
    @Column(name="status")
	public String status;
}
