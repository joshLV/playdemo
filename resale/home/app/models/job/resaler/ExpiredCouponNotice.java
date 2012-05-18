package models.job.resaler;

import play.jobs.Job;
import play.jobs.On;

/**
 * 每天12点发送短信提醒1周后过期的券
 *
 * @author 12-5-18
 * Time: 下午2:57
 */
@On("0 0 12 * * ?")
public class ExpiredCouponNotice extends Job {

    @Override
    public void doJob(){

    }
}
