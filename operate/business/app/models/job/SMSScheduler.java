package models.job;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.SendSMSInfo;
import models.sales.SendSMSTask;
import models.sms.SMSUtil;
import org.apache.commons.lang.StringUtils;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-12
 * Time: 上午11:34
 * To change this template use File | Settings | File Templates.
 */

@OnApplicationStart(async = true)
@Every("1mn")
public class SMSScheduler extends Job {
    @Override
    public void doJob() throws ParseException {
        List<SendSMSTask> smsTaskList = SendSMSTask.find("deleted=?", DeletedStatus.UN_DELETED).fetch();
        for (SendSMSTask st : smsTaskList) {
            Date currentDate = new Date();
//            System.out.println("st.scheduledTime"+st.scheduledTime);
//            System.out.println("currentDate"+currentDate);

            if (st.finished != st.total && st.scheduledTime != null && st.scheduledTime.before(currentDate)) {
                List<SendSMSInfo> smsList = SendSMSInfo.find("sendAt=null and deleted=? and taskNo=? ", DeletedStatus.UN_DELETED, st.taskNo).fetch();
                for (SendSMSInfo s : smsList) {
                    try {
//                        if (s.sendAt == null) {
                        System.out.println("asdfasdfasdfasdf");
                        st.finished = st.finished + 1L;
                        st.unfinished = st.unfinished - 1L;
                        System.out.println("st.finished" + st.finished);
                        System.out.println("st.unfinished" + st.unfinished);
                        s.sendAt = new Date();
                        //                        SMSUtil.send(s.text, s.mobile);
                        s.save();
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
