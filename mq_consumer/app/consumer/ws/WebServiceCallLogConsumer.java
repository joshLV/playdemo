package consumer.ws;

import models.RabbitMQConsumerWithTx;
import models.journal.WebServiceCallLog;

/**
 * User: tanglq
 * Date: 13-1-17
 * Time: 下午12:01
 */
public class WebServiceCallLogConsumer extends RabbitMQConsumerWithTx<WebServiceCallLog> {
    @Override
    public void consumeWithTx(WebServiceCallLog webServiceCallLog) {
        
    }

    @Override
    protected Class getMessageType() {
        return WebServiceCallLog.class;
    }

    @Override
    protected String queue() {
        return null;
    }
}
