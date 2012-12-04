package models.job;

import models.sales.SendSMSInfo;
import models.sales.SendSMSTask;
import models.sms.SMSUtil;
import play.jobs.Job;
import play.jobs.On;
import play.jobs.OnApplicationStart;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * User: wangjia
 * Date: 12-9-12
 * Time: 上午11:34
 */
@OnApplicationStart(async = true)
@On("0 * * * * ?")
//@Every("1mn")
public class SMSScheduler extends Job {
    @Override
    public void doJob() throws ParseException {
        System.out.println("))))))))))   Enter method SMSScheduler.doJob");

        List<SendSMSTask> smsTaskList = SendSMSTask.findUnDeleted();
        Date currentDate;
        for (SendSMSTask st : smsTaskList) {
            currentDate = new Date();

            if (st.finished.longValue() != st.total.longValue() && st.scheduledTime != null && st.scheduledTime.before(currentDate)) {
                List<SendSMSInfo> smsList = SendSMSInfo.findUnDeleted(st.taskNo);
                for (SendSMSInfo smsInfo : smsList) {
                    try {
                        st.finished = st.finished + 1L;
                        st.unfinished = st.unfinished - 1L;

                        smsInfo.sendAt = new Date();
                        SMSUtil.send(smsInfo.text, smsInfo.mobile);
                        smsInfo.save();
                        st.save();
                    } catch (Exception e) {
                        break;
                    }
                }
            }
        }
    }

}
