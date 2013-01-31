package models.order;

import org.apache.commons.lang.builder.ToStringBuilder;
import play.Play;
import util.mq.MQPublisher;

import java.io.Serializable;

/**
 * 包装处理订单和券相关的消息.
 */
public class OrderECouponMessage implements Serializable {

    private static final long serialVersionUID = 706323206329883135L;

    public static final String MQ_KEY = Play.mode.isProd() ? "send_order_sms" : "send_order_sms_dev";

    public Long orderItemId;

    public Long eCouponId;

    /**
     * 接收手机.
     */
    public String phone;

    /**
     * 备注.
     */
    public String remark;

    /**
     * 操作人名称.
     */
    public String operator;

    private OrderECouponMessage() {
        // 禁止直接创建对象
    }

    public OrderECouponMessage(Long _orderItemId, Long _eCouponId, String _phone, String _remark) {
        this.orderItemId = _orderItemId;
        this.eCouponId = _eCouponId;
        this.phone = _phone;
        this.remark = _remark;
    }

    public static OrderECouponMessage with(OrderItems orderItems) {
        OrderECouponMessage message = new OrderECouponMessage();
        message.orderItemId = orderItems.id;
        message.phone = orderItems.phone;
        message.operator = "Default";
        return message;
    }

    public static OrderECouponMessage with(ECoupon eCoupon) {
        OrderECouponMessage message = new OrderECouponMessage();
        message.orderItemId = eCoupon.orderItems.id;
        message.eCouponId = eCoupon.id;
        message.phone = eCoupon.orderItems.phone;
        message.operator = "Default";
        return message;
    }

    public OrderECouponMessage phone(String value) {
        this.phone = value;
        return this;
    }

    public OrderECouponMessage remark(String value) {
        this.remark = value;
        return this;
    }

    public OrderECouponMessage operator(String value) {
        this.operator = value;
        return this;
    }

    public void sendToMQ() {
        MQPublisher.publish(MQ_KEY, this);
    }

    public static OrderECouponMessage withOrderItemIdPhone(Long orderItemId, String phone, String remark) {
        return new OrderECouponMessage(orderItemId, null, phone, remark);
    }

    public static OrderECouponMessage withOrderItemId(Long orderItemId, String remark) {
        return new OrderECouponMessage(orderItemId, null, null, remark);
    }

    public static OrderECouponMessage withECouponIdPhone(Long eCouponId, String phone, String remark) {
        return new OrderECouponMessage(null, eCouponId, phone, remark);
    }

    public static OrderECouponMessage withECouponId(Long eCouponId, String remark) {
        return new OrderECouponMessage(null, eCouponId, null, remark);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("orderItemId", orderItemId).
                append("eCouponId", eCouponId).
                append("phone", phone).
                append("remark", remark).
                append("operator", operator).
                toString();
    }
}
