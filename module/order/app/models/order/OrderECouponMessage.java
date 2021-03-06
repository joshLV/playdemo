package models.order;

import com.google.common.collect.Lists;
import extension.order.OrderECouponSMSContext;
import extension.order.OrderECouponSMSInvocation;
import models.mq.QueueIDMessage;
import models.resale.Resaler;
import models.sales.Goods;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import play.Logger;
import play.Play;
import util.extension.DefaultAction;
import util.extension.ExtensionInvoker;
import util.extension.ExtensionResult;
import util.mq.MQPublisher;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 包装处理订单和券相关的消息.
 */
public class OrderECouponMessage extends QueueIDMessage implements Serializable {

    private static final long serialVersionUID = 793732320988395L;

    public static final String MQ_KEY = Play.mode.isProd() ? "order.v2.sms" : "order.v2.sms_dev";
    public static final String SMS_EXPIRE_FORMAT = "M月d日";

    // 默认的生成券短信格式.
    private static DefaultAction<OrderECouponSMSContext> defaultSmsAction = new DefaultAction<OrderECouponSMSContext>() {
        @Override
        public ExtensionResult execute(OrderECouponSMSContext ctx) {
            if (ctx.getSmsContent() == null) {
                StringBuilder sb = new StringBuilder();
                Goods goods = ctx.getGoods();
                sb.append((StringUtils.isNotEmpty(goods.title) ? goods.title : goods.shortName).replaceFirst("^尊享","")
                        .replaceFirst("^享受","").replaceFirst("^享",""))
                        .append(ctx.couponInfo)
                        .append(ctx.notes)
                        .append("至").append(ctx.expiredDate).append("有效,")
                        .append("客服4006865151");
                ctx.setSmsContent(sb.toString());
            }
            return ExtensionResult.SUCCESS;
        }
    };

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

    public Integer lockVersion;

    private OrderECouponMessage() {
        // 禁止直接创建对象
    }

    @Override
    public String messageId() {
        if (eCouponId != null && eCouponId > 0) {
            return MQ_KEY + "_ECOUPON_" + this.eCouponId;
        }
        return MQ_KEY + "_ITEM_" + this.orderItemId;
    }

    public static OrderECouponMessage with(OrderItems orderItems) {
        OrderECouponMessage message = new OrderECouponMessage();
        message.orderItemId = orderItems.id;
        message.phone = orderItems.phone;
        message.operator = "Default";
        message.lockVersion = 0;
        return message;
    }

    public static OrderECouponMessage with(ECoupon eCoupon) {
        OrderECouponMessage message = new OrderECouponMessage();
        message.orderItemId = eCoupon.orderItems.id;
        message.eCouponId = eCoupon.id;
        message.phone = eCoupon.orderItems.phone;
        message.operator = "Default";
        message.lockVersion = eCoupon.lockVersion;
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

    /**
     * 得到订单短信内容（单条)
     *
     * @return
     */
    public static OrderECouponSMSContext getOrderSMSMessage(ECoupon coupon) {
        if (!coupon.canSendSMSByOperate()) {  //检查是否可发短信
            Logger.info("ECoupon(id:" + coupon.id + ") stats is not UNCONSUMED, but was " + coupon.status + ", " +
                    "cann't send SMS.");
            return null;
        }
        List<ECoupon> eCoupons = new ArrayList<>();
        eCoupons.add(coupon);
        return getSMSContent(coupon.orderItems, eCoupons);
    }

    /**
     * 得到此订单发送购买短信的内容.
     *
     * @return
     */
    public static OrderECouponSMSContext[] getOrderSMSMessage(OrderItems orderItems) {
        if (orderItems.order.status != OrderStatus.PAID) {
            Logger.info("OrderItem(" + orderItems.id + ").order Status is NOT PAID, but was:" + orderItems.order.status);
            return null;  //未支付时不能发短信.
        }

        //京东的不发短信邮件等提示，因为等会儿京东会再次主动通知我们发短信
        if (orderItems.order.getResaler().loginName.equals(Resaler.JD_LOGIN_NAME)) {
            // do nothing. NOW!
        }

        if (orderItems.goods.isLottery != null && orderItems.goods.isLottery) {
            //抽奖商品不发短信邮件等提示
            Logger.info("goods(id:" + orderItems.goods.id + " is Lottery!");
            return null;
        }


        List<ECoupon> eCoupons = orderItems.getECoupons();

        List<ECoupon> needSendECoupons = new ArrayList<>();
        for (ECoupon e : eCoupons) {
            //过滤一下得到需要发送短信的券
            if (e.canSendSMSByOperate()) {
                needSendECoupons.add(e);
            }
        }
        if (needSendECoupons.size() == 0) {
            Logger.info("OrderItem(" + orderItems.id + ") does NOT contains any need Send ECoupons!");
            return null;
        }

        ECoupon firstECoupon = eCoupons.get(0);
        // 默认每18张券一个长短信
        int smsCouponSize = 18;
        if (StringUtils.isNotBlank(firstECoupon.eCouponPassword)) {
            smsCouponSize = 8;  //有密码是8张券一个短信
        }
        List<List<ECoupon>> splitECoupons = Lists.partition(needSendECoupons, smsCouponSize);
        OrderECouponSMSContext[] smsContents = new OrderECouponSMSContext[splitECoupons.size()];
        for (int i = 0; i < splitECoupons.size(); i++) {
            smsContents[i] = getSMSContent(orderItems, splitECoupons.get(i));
        }

        return smsContents;
    }

    private static OrderECouponSMSContext getSMSContent(OrderItems orderItems, List<ECoupon> eCoupons) {
        List<String> ecouponSNs = new ArrayList<>();
        ECoupon lastECoupon = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(SMS_EXPIRE_FORMAT);
        boolean appointmentSuccess = false;
        for (ECoupon e : eCoupons) {
            if (StringUtils.isNotBlank(e.eCouponPassword)) {
                StringBuilder sb = new StringBuilder();
                sb.append("券号").append(e.eCouponSn).append("密码").append(e.eCouponPassword);
                ecouponSNs.add(sb.toString());
            } else {
                if (e.appointmentDate != null) {
                    appointmentSuccess = true;
                }
                if (eCoupons.size() >= 1 && e.appointmentDate != null) {
                    ecouponSNs.add("券号" + e.eCouponSn + ",预约日期:" + dateFormat.format(e.appointmentDate) + "," + StringUtils.trimToEmpty(e.appointmentRemark));
                } else {
                    ecouponSNs.add("券号" + e.eCouponSn);
                }
            }
            lastECoupon = e;
        }
        if (lastECoupon == null) {
            Logger.info("OrderItem(" + orderItems.id + ") does NOT contains any ECoupons!");
            return null;
        }


        String couponInfo = StringUtils.join(ecouponSNs, ",");
        if (ecouponSNs.size() > 1) {
            couponInfo += "[共" + ecouponSNs.size() + "张]";
        }

        //预约商品或二次验证商品并且预约成功后不发送这段文字
        String note = ",";
        if (orderItems.goods.isOrder || (orderItems.goods.isSecondaryVerificationGoods() && !appointmentSuccess)) {
            // 需要预约的产品
            note += "需预约,";
        }

        String expiredDate = dateFormat.format(lastECoupon.expireAt);
        OrderECouponSMSContext context = new OrderECouponSMSContext(eCoupons, couponInfo, note, expiredDate);

        ExtensionResult result = ExtensionInvoker.run(OrderECouponSMSInvocation.class, context, defaultSmsAction);

        Logger.info("generate SMS Content:" + result);

        return context;
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
