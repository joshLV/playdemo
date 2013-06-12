package models.jobs;

import models.jobs.annotation.JobDefine;
import play.Logger;
import play.jobs.Every;
import play.modules.redis.Redis;

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
        Redis.incr(getRedisRunKey()); //运行过一次即加1
    }

    public static String getRedisRunKey() {
        return "test.foo.job.run";
    }
}
