package factory.jobs;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.jobs.JobsDefine;
import models.jobs.JobsRunHistory;

import java.util.Date;

/**
 * User: tanglq
 * Date: 13-5-4
 * Time: 上午11:45
 */
public class JobsRunHistoryFactory extends ModelFactory<JobsRunHistory> {
    @Override
    public JobsRunHistory define() {
        JobsRunHistory jobsRunHistory = new JobsRunHistory();
        jobsRunHistory.jobsDefine = FactoryBoy.lastOrCreate(JobsDefine.class);
        jobsRunHistory.runnedAt = new Date();
        jobsRunHistory.status = "SUCCESS";
        return jobsRunHistory;
    }
}
