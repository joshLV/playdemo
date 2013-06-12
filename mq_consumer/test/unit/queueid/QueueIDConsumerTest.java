package unit.queueid;

import models.mq.QueueIDRunType;
import org.junit.Before;
import org.junit.Test;
import play.modules.redis.Redis;
import play.test.UnitTest;
import util.mq.MQPublisher;

/**
 * QueueID Consumer测试.
 */
public class QueueIDConsumerTest extends UnitTest {
    SampleQueueIDMessage message1, message2;
    SampleQueueIDMessageConsumer consumer;

    @Before
    public void setUp() throws Exception {
        consumer = new SampleQueueIDMessageConsumer();
    }

    @Test
    public void testOnlyRunFirst() throws Exception {
        // 发送消息
        message1 = new SampleQueueIDMessage(32l, QueueIDRunType.ONLY_RUN_FIRST);
        Redis.del(new String[]{message1.getCountKey(), message1.getRunMessageKey()}); //清除运行记录
        MQPublisher.recordMessage(message1);
        message2 = new SampleQueueIDMessage(32l, QueueIDRunType.ONLY_RUN_FIRST);
        MQPublisher.recordMessage(message2);

        consumer.consume(message1);
        consumer.consume(message2);

        // 同一id的message只应该执行过一次
        assertEquals("1", Redis.get(message1.getCountKey()));
        assertEquals(message1.getUuid(), Redis.get(message2.getRunMessageKey()));
    }

    @Test
    public void testLastInFirstRun() throws Exception {
        // 发送消息
        message1 = new SampleQueueIDMessage(32l, QueueIDRunType.LAST_IN_FIRST_RUN);
        Redis.del(new String[]{message1.getCountKey(), message1.getRunMessageKey()}); //清除运行记录
        MQPublisher.recordMessage(message1);
        message2 = new SampleQueueIDMessage(32l, QueueIDRunType.LAST_IN_FIRST_RUN);
        MQPublisher.recordMessage(message2);

        consumer.consume(message1);
        consumer.consume(message2);

        // 同一id的message只应该执行过一次
        assertEquals("1", Redis.get(message1.getCountKey()));
        assertEquals(message2.getUuid(), Redis.get(message1.getRunMessageKey()));
    }

    @Test
    public void testRunInSequence() throws Exception {
        // 发送消息
        message1 = new SampleQueueIDMessage(32l, QueueIDRunType.LAST_IN_FIRST_RUN);
        Redis.del(new String[]{message1.getCountKey(), message1.getRunMessageKey()}); //清除运行记录
        MQPublisher.recordMessage(message1);
        consumer.consume(message1);

        message2 = new SampleQueueIDMessage(32l, QueueIDRunType.LAST_IN_FIRST_RUN);
        MQPublisher.recordMessage(message2);
        consumer.consume(message2);

        assertEquals("2", Redis.get(message1.getCountKey()));
        assertEquals(message2.getUuid(), Redis.get(message1.getRunMessageKey()));
    }
}
