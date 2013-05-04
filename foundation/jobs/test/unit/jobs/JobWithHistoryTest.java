package unit.jobs;

import factory.FactoryBoy;
import models.jobs.FooJob;
import models.jobs.JobsMessage;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.mq.MockMQ;

/**
 * User: tanglq
 * Date: 13-5-4
 * Time: 下午1:32
 */
public class JobWithHistoryTest extends UnitTest {
    @Before
    public void setUp() throws Exception {
        FactoryBoy.deleteAll();
        MockMQ.clear();
    }

    @Test
    public void testFooJob() throws Exception {
        FooJob job = new FooJob();
        job.doJob();

        JobsMessage jobsMessage = (JobsMessage) MockMQ.getLastMessage(JobsMessage.MQ_KEY);
        assertEquals(FooJob.class.getName(), jobsMessage.className);
        assertEquals("Foo测试", jobsMessage.title);
    }
}
