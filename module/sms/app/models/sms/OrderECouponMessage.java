package models.sms;

import java.io.Serializable;

/**
 * 包装处理订单和券相关的消息.
 */
public class OrderECouponMessage implements Serializable {

    public Long orderId;

    public Long orderItemId;

    public Long eCouponId;

    public OrderECouponMessage(Long orderId, Long orderItemId, Long eCouponId) {
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.eCouponId = eCouponId;
    }
}
