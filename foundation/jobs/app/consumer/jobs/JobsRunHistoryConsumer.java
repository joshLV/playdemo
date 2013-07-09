package consumer.jobs;

import models.jobs.JobsDefine;
import models.jobs.JobsMessage;
import models.jobs.JobsRunHistory;
import models.mq.RabbitMQConsumerWithTx;
import play.Logger;
import play.jobs.OnApplicationStart;

/**
 * 保存Job运行历史的MQ Consumer.
 */
@OnApplicationStart(async = true)
public class JobsRunHistoryConsumer extends RabbitMQConsumerWithTx<JobsMessage> {

    @Override
    public void consumeWithTx(JobsMessage jobsMessage) {
        Logger.info("JobsRunHistoryConsumer: job run:" + jobsMessage.className);
        JobsDefine jobsDefine = jobsMessage.toJobsDefine().load();

        jobsDefine.lastRunHistory = null;
        jobsDefine.save();
        // 删除指定分钟数以前的记录
        JobsRunHistory.deleteBeforeItem(jobsDefine, jobsMessage.retainHistoryMinutes);

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
