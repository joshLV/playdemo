package models.order;

import models.sales.Goods;
import play.db.jpa.Model;

import javax.persistence.*;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: pwg
 * Date: 12-3-5
 * Time: 下午5:16
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "e_coupon")
public class ECoupon extends Model {

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="order_id",nullable=true)
    public Orders order;

    @ManyToOne
    Goods goods;

    @Column(name="e_coupon_sn")
    public String eCouponSn;

    @Column(name="e_coupon_price")
    public Float eCouponPrice;

    @Column(name="refund_price")
    public Float refundPrice;

    @Column(name="created_at")
    @Temporal(TemporalType.DATE)
    public Date createdAt;

    @Column(name="consumed_at")
    public Date consumedAt;

    @Column(name="refund_at")
    public Date refundAt;
    
    @Column(name="buy_number")
    public int buyNumber;
    
    @Enumerated(EnumType.STRING)
    public ECouponStatus status;
}
