package unit.consumer;

import consumer.jobs.JobsRunHistoryConsumer;
import factory.FactoryBoy;
import models.jobs.JobsDefine;
import models.jobs.JobsMessage;
import models.jobs.JobsRunHistory;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.DateHelper;

import java.util.Date;

/**
 * User: tanglq
 * Date: 13-5-4
 * Time: 下午12:34
 */
public class JobsRunHistoryConsumerTest extends UnitTest {

    @Before
    public void setUp() throws Exception {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testConsumeWithTx() throws Exception {
        assertEquals(0, JobsDefine.count());
        JobsMessage message = JobsMessage.forClass("test.Class")
                .title("测试").description("Hello")
                .runnedAt(new Date())
                .runStatus("SUCCESS")
                .runRemark("测试运行成功");
        JobsRunHistoryConsumer consumer = new JobsRunHistoryConsumer();
        consumer.consumeWithTx(message);

        assertEquals(1, JobsDefine.count());
        assertEquals(1, JobsRunHistory.count());

        // 加一张History记录
        JobsDefine jobsDefine = (JobsDefine) JobsDefine.findAll().get(0);
        JobsRunHistory history = new JobsRunHistory();
        history.jobsDefine = jobsDefine;
        history.runnedAt = DateHelper.beforeMinuts(50);
        history.save();

        assertEquals(2, JobsRunHistory.count());


        JobsMessage message2 = JobsMessage.forClass("test.Class")
                .title("测试").description("Hello")
                .runnedAt(new Date())
                .runStatus("SUCCESS")
                .retainHistoryMinutes(48)
                .runRemark("测试运行成功");
        consumer.consumeWithTx(message2);
        assertEquals(1, JobsDefine.count());
        assertEquals(2, JobsRunHistory.count());
    }
}
