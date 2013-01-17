package consumer.ws;

import models.RabbitMQConsumerWithTx;
import models.journal.WebServiceCallLog;
import models.journal.WebServiceCallLogData;
import models.journal.WebServiceCallType;
import play.db.jpa.JPA;
import play.jobs.OnApplicationStart;
import util.ws.WebServiceClient;

/**
 * 记录Web服务调用日志，通过MQ方式可以避免在出现错误时，不能保存日志。
 */
@OnApplicationStart(async = true)
public class WebServiceCallLogConsumer extends RabbitMQConsumerWithTx<WebServiceCallLogData> {
    @Override
    public void consumeWithTx(WebServiceCallLogData message) {
        WebServiceCallLog log = message.toModel();
        JPA.em().flush();
        WebServiceCallType.checkOrCreate(log.callType);
        log.save();
    }

    @Override
    protected Class getMessageType() {
        return WebServiceCallLogData.class;
    }

    @Override
    protected String queue() {
        return WebServiceClient.MQ_KEY;
    }
}
