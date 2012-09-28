package models.mail;

import models.journal.MQJournal;
import notifiers.GoodsOffSalesMails;
import notifiers.TuanCategoryMails;
import play.Logger;
import play.db.jpa.JPAPlugin;
import play.exceptions.MailException;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-28
 * Time: 下午5:45
 * To change this template use File | Settings | File Templates.
 */
@OnApplicationStart(async = true)
public class TuanCategoryMailConsumer extends RabbitMQConsumer<MailMessage> {
    private void saveJournal(String content) {
        JPAPlugin.startTx(false);
        new MQJournal(queue(), content).save();
        JPAPlugin.closeTx(false);
    }

    @Override
    protected void consume(MailMessage message) {
        try {
            TuanCategoryMails.notify(message);
            saveJournal(message.getContent() + " -- 发送成功");
        } catch (MailException e) {
            Logger.warn(e, "发送邮件(" + message.getOneRecipient() + ")时出现异常");
            String content = "发送邮件(" + message.getOneRecipient() + ")时出现异常:" + e.getMessage();
            saveJournal(content);
        }
    }

    @Override
    protected Class getMessageType() {
        return MailMessage.class;
    }

    @Override
    protected String queue() {
        return MailUtil.TUAN_CATEGORY_NOTIFY;
    }


}
