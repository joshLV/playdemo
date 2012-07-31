package models.mail;

import models.journal.MQJournal;
import notifiers.GoodsOffSalesMails;
import play.Logger;
import play.db.jpa.JPAPlugin;
import play.exceptions.MailException;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-31
 * Time: 上午11:18
 */
@OnApplicationStart(async = true)
public class GoodsOffSalesMailsConsumer extends RabbitMQConsumer<MailMessage> {

    /**
     * 保存发送记录
     *
     * @param content 消息
     */
    private void saveJournal(String content) {
        JPAPlugin.startTx(false);
        new MQJournal(queue(), content).save();
        JPAPlugin.closeTx(false);
    }

    @Override
    protected void consume(MailMessage message) {
        try {
            GoodsOffSalesMails.notify(message);
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
        return MailUtil.GOODS_OFF_SALES_NOTIFY;
    }
}
