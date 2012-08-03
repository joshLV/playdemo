package models.mail;

import models.journal.MQJournal;
import notifiers.MailSender;
import play.Logger;
import play.db.jpa.JPAPlugin;
import play.exceptions.MailException;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

/**
 * @author likang
 *         Date: 12-8-3
 */
public abstract class MailConsumer extends RabbitMQConsumer<MailMessage>{

    @Override
    protected void consume(MailMessage message) {
        try {
            MailSender.send(message);
            saveJournal("邮件发送成功 " + messageSummary(message));
        } catch (MailException e) {
            String summary = "邮件发送异常 " + messageSummary(message);
            Logger.warn(e, summary);
            saveJournal(summary + " " + e.getMessage());
        }
    }
    @Override
    protected Class getMessageType() {
        return MailMessage.class;
    }

    @Override
    protected abstract String queue();

    private void saveJournal(String content) {
        JPAPlugin.startTx(false);
        new MQJournal(queue(),content).save();
        JPAPlugin.closeTx(false);
    }

    private String messageSummary(MailMessage message) {
        StringBuilder summary = new StringBuilder("| ");
        for(String receiver : message.getRecipients()){
            summary.append(receiver).append(",");
        }
        summary.append(" | ");
        summary.append(message.getSubject()).append(" |");
        return summary.toString();
    }
}
