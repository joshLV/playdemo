package unit.queueid;

import models.mq.QueueIDMessage;
import models.mq.QueueIDRunType;

import java.io.Serializable;

/**
 * User: tanglq
 * Date: 13-6-12
 * Time: 下午3:29
 */
public class SampleQueueIDMessage extends QueueIDMessage implements Serializable {
    public final static String MQ_KEY = "sample.queueid.message";

    public Long orderId;

    public QueueIDRunType queueIDRunType;

    public SampleQueueIDMessage(Long l, QueueIDRunType runType) {
        orderId = l;
        queueIDRunType = runType;
    }

    @Override
    public String messageId() {
        return MQ_KEY + orderId;
    }

    @Override
    public QueueIDRunType queueIDRunType() {
        return queueIDRunType;
    }

    public String getCountKey() {
        return "test.queueid.count." + orderId;
    }


    public String getRunMessageKey() {
        return "test.queueid.runed." + orderId;
    }
}
