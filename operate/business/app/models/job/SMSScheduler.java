package models.job;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.SendSMSInfo;
import models.sales.SendSMSTask;
import models.sms.SMSUtil;
import play.jobs.Every;
import play.jobs.Job;
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
@Every("1mn")
public class SMSScheduler extends Job {
    @Override
    public void doJob() throws ParseException {
        List<SendSMSTask> smsTaskList = SendSMSTask.findUnDeleted();
        for (SendSMSTask st : smsTaskList) {
            Date currentDate = new Date();

            if (st.finished.longValue() != st.total.longValue() && st.scheduledTime != null && st.scheduledTime.before(currentDate)) {
                List<SendSMSInfo> smsList = SendSMSInfo.find("sendAt=null and deleted=? and taskNo=? ", DeletedStatus.UN_DELETED, st.taskNo).fetch();
                for (SendSMSInfo smsInfo : smsList) {
                    try {
//                        if (smsInfo.sendAt == null) {

                        st.finished = st.finished + 1L;
                        st.unfinished = st.unfinished - 1L;

                        smsInfo.sendAt = new Date();
                        SMSUtil.send(smsInfo.text, smsInfo.mobile);
                        smsInfo.save();
                        st.save();
//                        }
                    } catch (Exception e) {
                        break;
                    }
                }
            }

        }

    }

}
