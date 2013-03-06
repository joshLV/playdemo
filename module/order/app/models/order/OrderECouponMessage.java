package models.order;

import models.accounts.AccountType;
import models.resale.Resaler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import play.Logger;
import play.Play;
import util.mq.MQPublisher;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * 得到订单短信内容（单条)
     *
     * @return
     */
    public static String getOrderSMSMessage(ECoupon coupon) {

        if (!coupon.canSendSMSByOperate()) {  //检查是否可发短信
            Logger.info("ECoupon(id:" + coupon.id + ") stats is not UNCONSUMED, but was " + coupon.status + ", cann't send SMS.");
            return null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(Order.COUPON_EXPIRE_FORMAT);

        String note = ",";
        if (coupon.goods.isOrder) {
            // 需要预约的产品
            if (coupon.appointmentDate != null) {
                note = ",预约于" + dateFormat.format(coupon.appointmentDate) + "到店消费,";
                if (StringUtils.isNotBlank(coupon.appointmentRemark)) {
                    note += coupon.appointmentRemark + ",";
                }
            } else {
                // 还没有预约
                note = ",此产品需预约,";
            }
        }
        String message = (StringUtils.isNotEmpty(coupon.goods.title) ? coupon.goods.title : coupon.goods.shortName) +
                "券号";
        if (StringUtils.isNotBlank(coupon.eCouponPassword)) {
            message += coupon.eCouponSn + "," + "密码" + coupon.eCouponPassword;
        } else {
            message += coupon.eCouponSn;
        }
        message += note + "截止" + dateFormat.format(coupon.expireAt) + "客服4006262166";
        // 重定义短信格式 - 58团
        if (AccountType.RESALER.equals(coupon.order.userType) && coupon.order.getResaler().loginName.equals(Resaler.WUBA_LOGIN_NAME)) {

            message = "【58团】" + (StringUtils.isNotEmpty(coupon.goods.title) ? coupon.goods.title : coupon.goods.shortName) +
                    "由58合作商家【一百券】提供,一百" + coupon.eCouponSn + note +
                    "有效期至" + dateFormat.format(coupon.expireAt) + "客服4007895858";
        }

        return message;
    }

    /**
     * 得到此订单发送购买短信的内容.
     *
     * @return
     */
    public static String getOrderSMSMessage(OrderItems orderItems) {
        if (orderItems.order.status != OrderStatus.PAID) {

            Logger.info("OrderItem(" + orderItems.id + ").order Status is NOT PAID, but was:" + orderItems.order.status);
            return null;  //未支付时不能发短信.
        }

        //京东的不发短信邮件等提示，因为等会儿京东会再次主动通知我们发短信
        if (AccountType.RESALER.equals(orderItems.order.userType)
                && orderItems.order.getResaler().loginName.equals(Resaler.JD_LOGIN_NAME)) {
            // do nothing. NOW!
        }

        if (orderItems.goods.isLottery != null && orderItems.goods.isLottery) {
            //抽奖商品不发短信邮件等提示
            Logger.info("goods(id:" + orderItems.goods.id + " is Lottery!");
            return null;
        }

        List<String> ecouponSNs = new ArrayList<>();
        ECoupon lastECoupon = null;

        for (ECoupon e : orderItems.getECoupons()) {
            if (StringUtils.isNotBlank(e.eCouponPassword)) {
                StringBuilder sb = new StringBuilder();
                sb.append("券号" + e.eCouponSn).append("密码").append(e.eCouponPassword);
                ecouponSNs.add(sb.toString());
            } else {
                ecouponSNs.add("券号" + e.eCouponSn);
            }
            lastECoupon = e;
        }

        if (lastECoupon == null) {
            Logger.info("OrderItem(" + orderItems.id + ") does NOT contains any ECoupons!");
            return null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(Order.COUPON_EXPIRE_FORMAT);

        String ecouponStr = StringUtils.join(ecouponSNs, "，");

        String summary;
        if (ecouponSNs.size() > 1) {
            summary = "[共" + ecouponSNs.size() + "张]";
        } else {
            summary = "";
        }

        String note = ",";
        if (orderItems.goods.isOrder) {
            // 需要预约的产品
            note = ",此产品需预约,";
        }
        String message = (StringUtils.isNotEmpty(orderItems.goods.title) ? orderItems.goods.title : orderItems.goods.shortName) +
                summary;
        message += ecouponStr;
        message += note + "截止" + dateFormat.format(lastECoupon.expireAt) + "客服4006262166";

        // 重定义短信格式 - 58团
        if (AccountType.RESALER.equals(orderItems.order.userType)
                && orderItems.order.getResaler().loginName.equals(Resaler.WUBA_LOGIN_NAME)) {

            message = "【58团】" + (StringUtils.isNotEmpty(orderItems.goods.title) ? orderItems.goods.title : orderItems.goods.shortName) +
                    summary + "由58合作商家【一百券】提供,一百券号" + ecouponStr + note +
                    "有效期至" + dateFormat.format(lastECoupon.expireAt) + "客服4007895858";
        }

        return message;
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
