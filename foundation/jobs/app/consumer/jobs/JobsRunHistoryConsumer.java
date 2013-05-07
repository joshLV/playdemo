package consumer.jobs;

import models.jobs.JobsDefine;
import models.jobs.JobsMessage;
import models.jobs.JobsRunHistory;
import models.jobs.RabbitMQConsumerWithTxOnJobs;
import play.jobs.OnApplicationStart;

/**
 * 保存Job运行历史的MQ Consumer.
 */
@OnApplicationStart(async = true)
public class JobsRunHistoryConsumer extends RabbitMQConsumerWithTxOnJobs<JobsMessage> {
    @Override
    public void consumeWithTx(JobsMessage jobsMessage) {
        JobsDefine jobsDefine = jobsMessage.toJobsDefine().load();

        JobsRunHistory history = new JobsRunHistory();
        history.jobsDefine = jobsDefine;
        history.runnedAt = jobsMessage.runnedAt;
        history.status = jobsMessage.runStatus;
        history.remark = jobsMessage.runRemark;
        history.runTimes = jobsMessage.runTimes;
        history.save();

        jobsDefine.lastRunHistory = history;
        jobsDefine.save();
    }

    @Override
    protected Class getMessageType() {
        return JobsMessage.class;
    }

    @Override
    protected String queue() {
        return JobsMessage.MQ_KEY;
    }
}
