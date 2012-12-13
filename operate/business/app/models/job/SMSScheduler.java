package models.job;

import models.sales.SendSMSInfo;
import models.sales.SendSMSTask;
import models.sms.SMSUtil;
import org.apache.commons.collections.CollectionUtils;
import play.jobs.Every;
import play.jobs.Job;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * User: wangjia
 * Date: 12-9-12
 * Time: 上午11:34
 */
@Every("1mn")
public class SMSScheduler extends Job {
    @Override
    public void doJob() throws ParseException {
        List<SendSMSTask> smsTaskList = SendSMSTask.findUnDeleted();
        Date currentDate;
        if (CollectionUtils.isEmpty(smsTaskList)) {
            return;
        }
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
