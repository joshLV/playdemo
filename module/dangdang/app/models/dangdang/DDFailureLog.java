package models.dangdang;

import models.order.ECoupon;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.util.Date;

/**
 * 当当接口调用失败日志记录.
 * <p/>
 * User: sujie
 * Date: 9/26/12
 * Time: 3:19 PM
 */
@Entity
@Table(name = "dd_failure_log")
public class DDFailureLog extends Model {
    @Column(name = "e_coupon_id")
    public Long eCouponId;

    @Column(name = "order_id")
    public Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_code")
    public ErrorCode errorCode;

    public String desc;

    @Column(name = "created_at")
    public Date createdAt;

    public DDFailureLog(ECoupon eCoupon, Response response) {
        if (eCoupon != null) {
            this.eCouponId = eCoupon.id;
            this.orderId = eCoupon.order.id;
        }
        if (response != null) {
            this.errorCode = response.errorCode;
            this.desc = response.desc;
        }
    }
}
