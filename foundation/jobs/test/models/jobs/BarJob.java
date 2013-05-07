package models.jobs;

import models.jobs.annotation.JobDefine;
import play.Logger;
import play.jobs.On;

/**
 * User: tanglq
 * Date: 13-5-7
 * Time: 上午10:17
 */
@JobDefine(title = "Bar测试")
@On("0 0 3 1 * ?")
public class BarJob  extends JobWithHistory {
    @Override
    public void doJobWithHistory() throws Exception {
        Logger.info("BarJob.doJobWithHistory(): run");
    }
}
