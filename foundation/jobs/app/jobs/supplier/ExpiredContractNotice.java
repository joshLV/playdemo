package jobs.supplier;

import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import play.jobs.On;

/**
 * User: yan
 * Date: 13-8-20
 * Time: 下午5:07
 */
@JobDefine(title = "商户合同预警检查", description = "10天后商户合同过期提醒")
@On("0 0 8 * * ?")
public class ExpiredContractNotice extends JobWithHistory {
    @Override
    public void doJobWithHistory() {

    }
}
