package jobs.sms;

import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.sales.SendSMSInfo;
import models.sales.SendSMSTask;
import models.sms.SMSMessage;
import org.apache.commons.collections.CollectionUtils;
import play.Play;
import play.jobs.Every;

import java.util.Date;
import java.util.List;

/**
 * User: wangjia
 * Date: 12-9-12
 * Time: 上午11:34
 */
@JobDefine(title="定时发送批量短信", description="定时发送批量短信")
@Every("5mn")
public class SMSScheduler extends JobWithHistory {
    @Override
    public void doJobWithHistory() throws Exception {
        if (Play.runingInTestMode()) {
            // 没写单元测试，这里就不跑了。这个逻辑如果要继续使用，可以放到Q里做。
            return;
        }
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
                        new SMSMessage(smsInfo.text, smsInfo.mobile).send();
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
