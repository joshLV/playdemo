package models.mail;

import models.journal.MQJournal;
import notifiers.CouponMails;
import play.Logger;
import play.db.jpa.JPAPlugin;
import play.exceptions.MailException;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

@OnApplicationStart(async = true)
public class CouponMailConsumer extends RabbitMQConsumer<CouponMessage> {


    /**
     * 保存发送记录
     *
     * @param message 消息
     * @param status  状态
     * @param serial  成功序列号
     */
    private void saveJournal(CouponMessage message, int status, String info) {
        JPAPlugin.startTx(false);
        new MQJournal(queue(), message.getFullName() + " | " + message.getEmail() + " | " + status + "|" + message.getCoupons().size() + "个优惠码发送|" + info).save();
        JPAPlugin.closeTx(false);
    }
    
    @Override
    protected void consume(CouponMessage message) {
        try {
            CouponMails.notifyOrder(message);
            saveJournal(message, 0, "发送成功");
        } catch (MailException e) {
            Logger.warn(e, "发送邮件(" + message.getEmail() + ")时出现异常");
            saveJournal(message, -1, "发送邮件(" + message.getEmail() + ")时出现异常:" + e.getMessage());
        }
    }

    @Override
    protected Class getMessageType() {
        return CouponMessage.class;
    }

    @Override
    protected String queue() {
        return MailUtil.COUPON_MAIL_QUEUE_NAME;
    }

}
