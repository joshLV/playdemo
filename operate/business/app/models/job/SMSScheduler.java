package models.job;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.SendSMSInfo;
import models.sales.SendSMSTask;
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
                System.out.println("ininininini111111111");
                List<SendSMSInfo> smsList = SendSMSInfo.find("deleted=? and taskNo=?", DeletedStatus.UN_DELETED, st.taskNo).fetch();
                for (SendSMSInfo s : smsList) {
                    System.out.println("ininininini2222222222");
                    // if (!StringUtils.isBlank(s.sendAt.toString()))
                    if (s.sendAt == null) {
                        System.out.println("ininininini33333333");
                        System.out.println("send" + st.scheduledTime);
                        st.finished = st.finished - (long)1;
                        st.unfinished = st.unfinished + (long)1;
                        s.sendAt = new Date();
//            SMSUtil.send(s.text, s.mobile);
                        s.save();
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
