package consumer.order;

import jobs.dadong.DadongConsumptionRequest;
import jobs.dadong.DadongErSendToRequest;
import models.RabbitMQConsumerWithTx;
import models.order.ECoupon;
import models.order.ECouponHistoryMessage;
import models.order.OrderECouponMessage;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.sms.SMSException;
import models.sms.SMSFactory;
import models.sms.SMSMessage;
import models.sms.SMSProvider;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.jobs.OnApplicationStart;
import util.transaction.RemoteRecallCheck;
import util.transaction.TransactionCallback;
import util.transaction.TransactionRetry;

import java.util.List;

/**
 * 发送订单的短信，如果有多个短信，会一次发掉。
 */
@OnApplicationStart(async = true)
public class OrderSendSmsConsumer extends RabbitMQConsumerWithTx<OrderECouponMessage> {
    private final String SMS_TYPE = Play.configuration.getProperty("sms.type");
    private final String SMS_TYPE2 = Play.configuration.getProperty("sms2.type");

    private SMSProvider smsProvider = null;
    private SMSProvider smsProvider2 = null;

    public SMSProvider getSMSProvider() {
        if (smsProvider == null) {
            smsProvider = SMSFactory.getSMSProvider(SMS_TYPE);
        }
        return smsProvider;
    }

    public SMSProvider getSMSProvider2() {
        if (smsProvider2 == null) {
            smsProvider2 = SMSFactory.getSMSProvider(SMS_TYPE2);
        }
        return smsProvider2;
    }

    @Override
    public void consumeWithTx(final OrderECouponMessage message) {
        if (message.eCouponId != null) {
            RemoteRecallCheck.setId("OrderSMS_ECoupon_" + message.eCouponId);
        } else {
            RemoteRecallCheck.setId("OrderSMS_OrderItem_" + message.orderItemId);
        }
        // 使用事务重试
        Boolean success = TransactionRetry.run(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction() {
                return doSendSms(message);
            }
        });
        RemoteRecallCheck.cleanUp();
        if (success == null || !success) {
            throw new RuntimeException("Retry later.");
        }
    }

    private Boolean doSendSms(OrderECouponMessage message) {
        if (message.eCouponId != null) {
            ECoupon ecoupon = ECoupon.findById(message.eCouponId);
            if (ecoupon != null && ecoupon.canSendSMSByOperate()) {
                sendECouponSMS(ecoupon, message);
            } else {
                if (ecoupon == null) {
                    Logger.info("Can NOT found ECoupon(id:" + message.eCouponId + "), retry later.");
                } else {
                    Logger.info("The ECoupon(id:" + message.eCouponId + ").status is " + ecoupon.status +
                            ", retry later.");
                }
                throw new RuntimeException("Retry later.");
            }
        } else if (message.orderItemId != null) {
            OrderItems orderItems = OrderItems.findById(message.orderItemId);
            if (orderItems != null && orderItems.order.status == OrderStatus.PAID) {
                sendOrderItemsSMS(orderItems, message);
            } else {
                if (orderItems == null) {
                    Logger.info("Can NOT found OrderItems(id:" + message.orderItemId + "), retry later.");
                } else {
                    Logger.info("The OrderItems(id:" + message.orderItemId + ").status is " + orderItems.order.status +
                            ", retry later.");
                }
                throw new RuntimeException("Retry later.");
            }
        }
        return Boolean.TRUE;
    }

    private void sendOrderItemsSMS(OrderItems orderItems, OrderECouponMessage message) {

        try {
            List<ECoupon> ecoupons = orderItems.getECoupons();
            if (ecoupons.size() == 0) {
                Logger.info("OrderItems(id:" + orderItems.id + ").ecoupons is EMPTY, " +
                        "Will NOT Send SMS.");
                return;
            }

            //处理指定手机号的情况
            String phone = orderItems.phone;
            String remark = message.remark == null ? "" : message.remark;
            if (StringUtils.isNotBlank(message.phone) && !message.phone.equals(phone)) {
                phone = message.phone;
                remark += " 发到新手机" + phone;
            }

            // 大东票务券发送
            if (DadongConsumptionRequest.check(orderItems)) {
                if (DadongConsumptionRequest.isResendTo(orderItems)) {
                    DadongErSendToRequest.resend(orderItems, phone);
                } else {
                    DadongConsumptionRequest.sendOrder(orderItems);
                }
                return;
            }

            String[] smsMessages = OrderECouponMessage.getOrderSMSMessage(orderItems);

            if (smsMessages == null) {
                Logger.info("OrderItems(id:" + orderItems.id + ").getOrderSMSMessage() == null, " +
                        "Will NOT Send SMS.");
                return;
            }

            for (String msg : smsMessages) {
                try {
                    getSMSProvider().send(new SMSMessage(msg, phone, ecoupons.get(0).replyCode));
                } catch (Exception e1) {
                    Logger.info("Send SMS failed use " + SMS_TYPE + ", try " + SMS_TYPE2);
                    getSMSProvider2().send(new SMSMessage(msg, phone, ecoupons.get(0).replyCode));
                }
            }

            for (ECoupon ecoupon : ecoupons) {
                if (!ecoupon.canSendSMSByOperate()) {
                    continue;  //不能发短信的不用记录历史
                }
                // 如果没有出现异常，则记录一下发送历史
                if (ecoupon.smsSentCount == null) {
                    ecoupon.smsSentCount = 0;
                }
                ecoupon.smsSentCount += 1;
                ecoupon.save();
                ECouponHistoryMessage.with(ecoupon).phone(phone).operator(message.operator).remark(remark).sendToMQ();
            }
        } catch (SMSException e) {
            Logger.error("Sms2SenderConsumer: send message" + message + " failed:" + e.getMessage());
            throw e;
        }
    }

    private void sendECouponSMS(ECoupon ecoupon, OrderECouponMessage message) {
        String msg = OrderECouponMessage.getOrderSMSMessage(ecoupon);


        if (msg == null) {
            Logger.info("ECoupon(id:" + ecoupon.id + ").getOrderSMSMessage() == null, Will NOT Send SMS.");
            return;
        }
        try {
            //处理指定手机号的情况
            String phone = ecoupon.orderItems.phone;
            String remark = message.remark == null ? "" : message.remark;
            if (StringUtils.isNotBlank(message.phone) && !message.phone.equals(phone)) {
                phone = message.phone;
                remark += " 发到新手机" + phone;
            }

            if (DadongConsumptionRequest.check(ecoupon.orderItems)) {
                if (DadongConsumptionRequest.isResendTo(ecoupon.orderItems)) {
                    DadongErSendToRequest.resend(ecoupon.orderItems, phone);
                } else {
                    DadongConsumptionRequest.sendOrder(ecoupon.orderItems);
                }
            }

            try {
                getSMSProvider().send(new SMSMessage(msg, phone, ecoupon.replyCode));
            } catch (Exception e1) {
                Logger.info("Send SMS failed use " + SMS_TYPE + ", try " + SMS_TYPE2);
                getSMSProvider2().send(new SMSMessage(msg, phone, ecoupon.replyCode));
            }
            // 如果没有出现异常，则记录一下发送历史
            if (ecoupon.smsSentCount == null) {
                ecoupon.smsSentCount = 0;
            }
            ecoupon.smsSentCount += 1;
            ecoupon.save();
            ECouponHistoryMessage.with(ecoupon).operator(message.operator).phone(phone)
                    .remark(remark).sendToMQ();
        } catch (SMSException e) {
            Logger.error("Sms2SenderConsumer: send message" + message + " failed:" + e.getMessage());
            throw e;
        }
    }

    @Override
    protected Class getMessageType() {
        return OrderECouponMessage.class;
    }

    @Override
    protected String queue() {
        return OrderECouponMessage.MQ_KEY;
    }

    @Override
    protected int retries() {
        return 10;  // 重试10次
    }
}
