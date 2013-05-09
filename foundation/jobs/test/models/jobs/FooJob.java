package models.jobs;

import models.jobs.annotation.JobDefine;
import play.Logger;
import play.jobs.Every;

/**
 * User: tanglq
 * Date: 13-5-4
 * Time: 下午1:30
 */
@JobDefine(title = "Foo测试", retainHistoryMinutes = 3600)
@Every("1h")
public class FooJob extends JobWithHistory {
    @Override
    public void doJobWithHistory() throws Exception {
        Logger.info("FooJob.doJobWithHistory(): run");
    }
}
