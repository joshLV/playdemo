package models.job;

import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-12
 * Time: 上午11:34
 * To change this template use File | Settings | File Templates.
 */

@OnApplicationStart(async=true)
@Every("1mn")
public class SMSScheduler extends Job {
    @Override
    public void doJob()
    {


    }


}
