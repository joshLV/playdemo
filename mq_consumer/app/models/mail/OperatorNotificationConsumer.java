package models.mail;

import models.journal.MQJournal;
import notifiers.FindPassWordMails;
import notifiers.OperatorMails;
import play.Logger;
import play.db.jpa.JPAPlugin;
import play.exceptions.MailException;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

/**
 * @author likang
 *         Date: 12-7-20
 *         Time: 下午5:10
 */
@OnApplicationStart(async = true)
public class OperatorNotificationConsumer extends RabbitMQConsumer<MailMessage> {

    /**
     * 保存发送记录
     *
     * @param content 消息
     */
    private void saveJournal(String content) {
        JPAPlugin.startTx(false);
        new MQJournal(queue(),content).save();
        JPAPlugin.closeTx(false);
    }

    @Override
    protected void consume(MailMessage message) {
        try {
            OperatorMails.notify(message);
            saveJournal(message.getContent() + " -- 发送成功");
        } catch (MailException e) {
            Logger.warn(e, "发送邮件(" + message.getOneRecipient() + ")时出现异常");
            String content  =  "发送邮件(" + message.getOneRecipient() + ")时出现异常:" + e.getMessage();
            saveJournal(content);
        }
    }

    @Override
    protected Class getMessageType() {
        return MailMessage.class;
    }

    @Override
    protected String queue() {
        return MailUtil.OPERATOR_NOTIFICATION;
    }
}
