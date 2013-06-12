package unit.jobs;

import factory.FactoryBoy;
import models.jobs.BarJob;
import models.jobs.FooJob;
import models.jobs.JobWithHistory;
import models.jobs.JobsMessage;
import org.junit.Before;
import org.junit.Test;
import play.modules.redis.Redis;
import play.test.UnitTest;
import util.mq.MockMQ;

/**
 * User: tanglq
 * Date: 13-5-4
 * Time: 下午1:32
 */
public class JobWithHistoryTest extends UnitTest {
    FooJob fooJob;
    @Before
    public void setUp() throws Exception {
        FactoryBoy.deleteAll();
        MockMQ.clear();
        fooJob = new FooJob();
        // 清除Redis运行历史
        JobWithHistory.cleanLastBeginRunAtForTest();
        Redis.del(new String[]{FooJob.getRedisRunKey()});
    }

    @Test
    public void testFooJob() throws Exception {
        fooJob.doJob();

        JobsMessage jobsMessage = (JobsMessage) MockMQ.getLastMessage(JobsMessage.MQ_KEY);
        assertEquals(FooJob.class.getName(), jobsMessage.className);
        assertEquals("Foo测试", jobsMessage.title);
        assertEquals("@Every(1h)", jobsMessage.scheduledInfo);
    }

    @Test
    public void testBarJob() throws Exception {
        BarJob job = new BarJob();
        job.doJob();

        JobsMessage jobsMessage = (JobsMessage) MockMQ.getLastMessage(JobsMessage.MQ_KEY);
        assertEquals(BarJob.class.getName(), jobsMessage.className);
        assertEquals("Bar测试", jobsMessage.title);
        assertEquals("@On(0 0 3 1 * ?)", jobsMessage.scheduledInfo);
    }

    @Test
    public void testOnlyOneJobsRun() throws Exception {
        fooJob.doJob();
        fooJob.doJob();

        // 只运行过一次
        assertEquals("1", Redis.get(FooJob.getRedisRunKey()));
    }
}
