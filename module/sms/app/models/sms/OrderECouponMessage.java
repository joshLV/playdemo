package models.sms;

import java.io.Serializable;

/**
 * 包装处理订单和券相关的消息.
 */
public class OrderECouponMessage implements Serializable {

    private static final long serialVersionUID = 706323206329883135L;

    public Long orderItemId;

    public Long eCouponId;

    public String phone;

    public String remark;

    public OrderECouponMessage(Long _orderItemId, Long _eCouponId, String _phone, String _remark) {
        this.orderItemId = _orderItemId;
        this.eCouponId = _eCouponId;
        this.phone = _phone;
        this.remark = _remark;
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
}
