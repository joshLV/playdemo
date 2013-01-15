package models.sms;

import java.io.Serializable;

/**
 * 包装处理订单和券相关的消息.
 */
public class OrderECouponMessage implements Serializable {

    private static final long serialVersionUID = 70632320639883135L;

    public Long orderItemId;

    public Long eCouponId;

    public String remark;

    public static OrderECouponMessage withOrderItemId(Long orderItemId, String remark) {
        OrderECouponMessage msg = new OrderECouponMessage();
        msg.orderItemId = orderItemId;
        msg.remark = remark;
        return msg;
    }

    public static OrderECouponMessage withECouponId(Long eCouponId, String remark) {
        OrderECouponMessage msg = new OrderECouponMessage();
        msg.eCouponId = eCouponId;
        msg.remark = remark;
        return msg;
    }
}
