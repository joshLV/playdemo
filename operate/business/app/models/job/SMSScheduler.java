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
    public void doJob() {
        List<SendSMSTask> smsTaskList = SendSMSTask.find("deleted=?", DeletedStatus.UN_DELETED).fetch();
        for (SendSMSTask st : smsTaskList) {


            Date date = new Date(new Date().getTime());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(date);

            if (st.finished != st.total && compare_date(currentTime, st.scheduledTime) == 1) {
                List<SendSMSInfo> smsList = SendSMSInfo.find("deleted=? and taskNo=?", DeletedStatus.UN_DELETED, st.taskNo).fetch();
                for (SendSMSInfo s : smsList) {
                    if (s.sendAt == null) {

                        st.finished = st.finished + 1L;
                        st.unfinished = st.unfinished - 1L;
                        System.out.println("st.finished" + st.finished);
                        System.out.println("st.unfinished" + st.unfinished);
                        s.sendAt = new Date();
                        SMSUtil.send(s.text, s.mobile);
                        s.save();
                        st.save();
                    }
                }
            }

        }

    }

    public static int compare_date(String DATE1, String DATE2) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date dt1 = df.parse(DATE1);
            Date dt2 = df.parse(DATE2);
            if (dt1.getTime() > dt2.getTime()) {
                System.out.println("dt1 在dt2前");
                return 1;
            } else if (dt1.getTime() < dt2.getTime()) {
                System.out.println("dt1在dt2后");
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }


}
