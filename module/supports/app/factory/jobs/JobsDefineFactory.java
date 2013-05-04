package factory.jobs;

import factory.ModelFactory;
import models.jobs.JobsDefine;

/**
 * User: tanglq
 * Date: 13-5-4
 * Time: 上午11:08
 */
public class JobsDefineFactory extends ModelFactory<JobsDefine> {
    @Override
    public JobsDefine define() {
        return JobsDefine.forClass("models.jobs.TestJobs")
                .title("测试Jobs")
                .description("用于测试")
                .scheduled("On(0 0 0 0 *)").load();
    }
}
