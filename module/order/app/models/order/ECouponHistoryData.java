package models.order;

import util.mq.MQPublisher;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;

/**
 * 用于通过MQ保存Coupon History的值对象.
 * User: tanglq
 * Date: 13-1-23
 * Time: 上午11:25
 */
public class ECouponHistoryData implements Serializable {

    private static final long serialVersionUID = 31693283113062L;

    public static final String MQ_KEY = "order.ecoupon.history";

    public Long eCouponId;

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
    public VerifyCouponType verifyType;

    /**
     * 券号原来状态
     */
    public ECouponStatus fromStatus;

    /**
     * 券号变更后状态
     */
    public ECouponStatus toStatus;

    private ECouponHistoryData() {
        // 不允许直接使用构造函数
    }

    public static ECouponHistoryData newInstance(ECoupon eCoupon) {
        ECouponHistoryData data = new ECouponHistoryData();
        data.eCouponId = eCoupon.id;
        data.createdAt = new Date();
        data.fromStatus = eCoupon.status;
        data.toStatus = eCoupon.status;
        return data;
    }

    public CouponHistory toModel() {
        CouponHistory couponHistory = new CouponHistory();

        couponHistory.coupon = ECoupon.findById(eCouponId);
        couponHistory.createdAt = createdAt;
        couponHistory.operator = operator;
        couponHistory.remark = remark;
        couponHistory.verifyType = verifyType;
        couponHistory.fromStatus = fromStatus;
        couponHistory.toStatus = toStatus;

        return couponHistory;
    }

    public ECouponHistoryData remark(String remark) {
        this.remark = remark;
        return this;
    }


    public ECouponHistoryData fromStatus(ECouponStatus status) {
        this.fromStatus = status;
        return this;
    }

    public ECouponHistoryData toStatus(ECouponStatus status) {
        this.fromStatus = status;
        return this;
    }

    public ECouponHistoryData verifyType(VerifyCouponType type) {
        this.verifyType = type;
        return this;
    }

    public ECouponHistoryData operator(String operator) {
        this.operator = operator;
        return this;
    }

    public void sendToMQ() {
        MQPublisher.publish(MQ_KEY, this);
    }
}
