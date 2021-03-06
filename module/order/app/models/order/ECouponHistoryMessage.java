package models.order;

import models.mq.QueueIDMessage;
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
public class ECouponHistoryMessage extends QueueIDMessage implements Serializable {

    private static final long serialVersionUID = 31693183903062L;

    public static final String MQ_KEY = "ecoupon.history";

    public Long eCouponId;

    @Column(name = "created_at")
    public Date createdAt;

    /**
     * 操作人
     */
    public String operator;

    /**
     * 备注，操作说明
     */
    public String remark;

    public String phone;

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

    /**
     * 订单ID.
     */
    public Long orderId;

    /**
     * 订单项ID.
     */
    public Long itemId;

    /**
     * 第三方券号
     */
    public String partnerCouponSn;
    /**
     * 券密码
     */
    public String couponPassword;

    /**
     * 预约时间
     */
    public Date appointmentDate;

    /**
     * 券号
     */
    public String couponSn;

    private ECouponHistoryMessage() {
        // 不允许直接使用构造函数
    }

    @Override
    public String messageId() {
        return MQ_KEY + "_" + this.eCouponId;
    }

    public static ECouponHistoryMessage with(ECoupon eCoupon) {
        ECouponHistoryMessage message = new ECouponHistoryMessage();
        message.eCouponId = eCoupon.id;
        message.orderId = eCoupon.order.id;
        message.itemId = eCoupon.orderItems.id;
        message.createdAt = new Date();
        message.fromStatus = eCoupon.status;
        message.toStatus = eCoupon.status;
        message.phone = eCoupon.orderItems.phone;
        message.partnerCouponSn = eCoupon.partnerCouponId;
        message.couponPassword = eCoupon.eCouponPassword;
        message.couponSn = eCoupon.eCouponSn;
        message.appointmentDate = eCoupon.appointmentDate;
        return message;
    }

    public CouponHistory toModel() {
        CouponHistory couponHistory = new CouponHistory();

        couponHistory.couponId = eCouponId;
        couponHistory.orderId = orderId;
        couponHistory.itemId = itemId;
        couponHistory.createdAt = createdAt;
        couponHistory.operator = operator;
        couponHistory.remark = remark;
        couponHistory.phone = phone;
        couponHistory.verifyType = verifyType;
        couponHistory.fromStatus = fromStatus;
        couponHistory.toStatus = toStatus;
        couponHistory.partnerCouponSn = partnerCouponSn;
        couponHistory.couponPassword = couponPassword;
        couponHistory.couponSn = couponSn;
        couponHistory.appointmentDate = appointmentDate;

        return couponHistory;
    }

    public ECouponHistoryMessage remark(String value) {
        this.remark = value;
        return this;
    }


    public ECouponHistoryMessage fromStatus(ECouponStatus value) {
        this.fromStatus = value;
        return this;
    }

    public ECouponHistoryMessage toStatus(ECouponStatus value) {
        this.toStatus = value;
        return this;
    }

    public ECouponHistoryMessage verifyType(VerifyCouponType value) {
        this.verifyType = value;
        return this;
    }


    public ECouponHistoryMessage phone(String value) {
        this.phone = value;
        return this;
    }

    public ECouponHistoryMessage operator(String value) {
        this.operator = value;
        return this;
    }

    public void sendToMQ() {
        MQPublisher.publish(MQ_KEY, this);
    }

    @Override
    public String toString() {
        return "ECouponHistoryMessage{" +
                "eCouponId=" + eCouponId +
                ", createdAt=" + createdAt +
                ", operator='" + operator + '\'' +
                ", remark='" + remark + '\'' +
                ", phone='" + phone + '\'' +
                ", verifyType=" + verifyType +
                ", fromStatus=" + fromStatus +
                ", toStatus=" + toStatus +
                ", orderId=" + orderId +
                ", itemId=" + itemId +
                '}';
    }
}
