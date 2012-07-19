package models.mail;

import models.journal.MQJournal;
import notifiers.CouponMails;
import play.Logger;
import play.db.jpa.JPAPlugin;
import play.exceptions.MailException;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

@OnApplicationStart(async = true)
public class CouponMailConsumer extends RabbitMQConsumer<MailMessage> {


    /**
     * 保存发送记录
     *
     * @param message 消息
     * @param status  状态
     * @param info 成功序列号
     */
    private void saveJournal(MailMessage message, int status, String info) {
        JPAPlugin.startTx(false);
        new MQJournal(queue(), message.getParam("full_name") + " | " + message.getOneRecipient() + " | " + status + "|" +  info).save();
        JPAPlugin.closeTx(false);
    }
    
    @Override
    protected void consume(MailMessage message) {
        try {
            CouponMails.notify(message);
            saveJournal(message, 0, "发送成功");
        } catch (MailException e) {
            Logger.warn(e, "发送邮件(" + message.getOneRecipient() + ")时出现异常");
            saveJournal(message, -1, "发送邮件(" + message.getOneRecipient() + ")时出现异常:" + e.getMessage());
        }
    }

    @Override
    protected Class getMessageType() {
        return MailMessage.class;
    }

    @Override
    protected String queue() {
        return MailUtil.COUPON_MAIL_QUEUE_NAME;
    }

}
