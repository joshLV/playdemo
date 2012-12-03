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
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-12
 * Time: 上午11:34
 */

@OnApplicationStart(async = true)
@Every("1mn")
//@On("0 * * * * ?")  //每天凌晨执行,自动取消过期十天的未付款的订单
public class SMSScheduler extends Job {
    @Override
    public void doJob() throws ParseException {
        System.out.println("DeletedStatus.UN_DELETED.ordinal():" + DeletedStatus.UN_DELETED.ordinal());
        List<SendSMSTask> smsTaskList = SendSMSTask.find("deleted=?", DeletedStatus.UN_DELETED).fetch();
        for (SendSMSTask st : smsTaskList) {
            Date currentDate = new Date();

            if (st.finished.longValue() != st.total.longValue() && st.scheduledTime != null && st.scheduledTime.before(currentDate)) {
                List<SendSMSInfo> smsList = SendSMSInfo.find("sendAt=null and deleted=? and taskNo=? ", DeletedStatus.UN_DELETED, st.taskNo).fetch();
                for (SendSMSInfo s : smsList) {
                    try {
//                        if (s.sendAt == null) {

                        st.finished = st.finished + 1L;
                        st.unfinished = st.unfinished - 1L;

                        s.sendAt = new Date();
                        SMSUtil.send(s.text, s.mobile);
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
