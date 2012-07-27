package models.mail;

import notifiers.EmailLoggerMails;
import play.Play;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

import java.util.ArrayList;

/**
 * @author likang
 *         Date: 12-7-27
 */
@OnApplicationStart(async = true)
public class EmailLoggerConsumer extends RabbitMQConsumer<MailMessage>{
    private String willSend = Play.configuration.getProperty("logger.email.will_send", "true");
    private String receiver = Play.configuration.getProperty("logger.email.receiver", null);
    @Override
    protected void consume(MailMessage message) {
        if(Play.runingInTestMode()){
            return;
        }
        if("true".equals(willSend)){
            if(receiver != null){
                message.setRecipients(new ArrayList<String>());
                message.addRecipient(receiver);
            }
            EmailLoggerMails.notify(message);
        }
    }

    @Override
    protected Class getMessageType() {
        return MailMessage.class;
    }

    @Override
    protected String queue() {
        return MailUtil.EMAIL_LOGGER;
    }
}
