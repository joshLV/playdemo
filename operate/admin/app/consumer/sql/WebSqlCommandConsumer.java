package consumer.sql;

import models.WebSqlCommand;
import models.WebSqlCommandMessage;
import play.Logger;
import play.db.jpa.JPAPlugin;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

/**
 * 记录SQL命令操作历史.
 */
@OnApplicationStart(async = true)
public class WebSqlCommandConsumer extends RabbitMQConsumer<WebSqlCommandMessage> {
    @Override
    public void consume(WebSqlCommandMessage webSqlCommandMessage) {
        Logger.info("process WebSqlCommandMessage:" + webSqlCommandMessage.sql);
        JPAPlugin.startTx(false);
        boolean rollback = false;
        try {
            WebSqlCommand command = WebSqlCommand.fromMessage(webSqlCommandMessage);
            command.save();
        } catch (RuntimeException e) {
            rollback = true;
            throw e;
        } finally {
            JPAPlugin.closeTx(rollback);
        }
    }

    @Override
    protected Class getMessageType() {
        return WebSqlCommandMessage.class;
    }

    @Override
    protected String queue() {
        return WebSqlCommandMessage.MQ_KEY;
    }
}
