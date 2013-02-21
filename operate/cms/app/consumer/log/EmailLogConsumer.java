package consumer.log;

import notifiers.LogMailSender;
import play.Logger;
import play.jobs.OnApplicationStart;
import play.modules.emaillogs.EmailLogMessage;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

/**
 * 用于发送错误日志邮件的Queue。
 *
 * 暂放在operate-cms模块，这样如果出错阻塞，不会影响到外部用户。
 */
@OnApplicationStart(async = true)
public class EmailLogConsumer extends RabbitMQConsumer<EmailLogMessage> {

    @Override
    protected void consume(EmailLogMessage emailLogMessage) {
        Logger.info("发送错误异常邮件[" + emailLogMessage.applicationName + "]:" + emailLogMessage.message);
        LogMailSender.sendLog(emailLogMessage);
    }

    @Override
    protected Class getMessageType() {
        return EmailLogMessage.class;
    }

    @Override
    protected String queue() {
        return EmailLogMessage.MQ_KEY;
    }
}
