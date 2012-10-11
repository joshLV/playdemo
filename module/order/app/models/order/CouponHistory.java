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
    @Column(name = "coupon_sn")
    public String couponSn;

    @Column(name = "created_at")
    public Date createdAt;
    /**
     * 操作人
     */
    public String operator;

    public String remark;
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

    public CouponHistory(String couponSn, String operator, String remark, ECouponStatus fromStatus, ECouponStatus toStatus,VerifyCouponType verifyType) {
        this.couponSn = couponSn;
        this.operator = operator;
        this.remark = remark;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.verifyType = verifyType;
        this.createdAt = new Date();
    }
}
