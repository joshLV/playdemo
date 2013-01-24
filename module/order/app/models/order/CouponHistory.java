package models.order;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-10-10
 * Time: 下午4:08
 */
@Entity
@Table(name = "coupon_history")
public class CouponHistory extends Model {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    public ECoupon coupon;

    @Column(name = "created_at")
    public Date createdAt;
    /**
     * 操作人
     */
    public String operator;

    public String remark;

    /**
     * 券使用的电话
     */
    public String phone;

    /**
     * 验证方式
     */
    @Enumerated(EnumType.STRING)
    public VerifyCouponType verifyType;
    /**
     * 券号原来状态
     */
    @Enumerated(EnumType.STRING)
    public ECouponStatus fromStatus;
    /**
     * 券号变更后状态
     */
    @Enumerated(EnumType.STRING)
    public ECouponStatus toStatus;

}
