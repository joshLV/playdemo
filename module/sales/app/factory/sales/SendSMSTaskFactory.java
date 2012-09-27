package factory.sales;

import com.uhuila.common.constants.DeletedStatus;
import factory.ModelFactory;
import models.sales.SendSMSInfo;
import models.sales.SendSMSTask;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-27
 * Time: 下午1:35
 * To change this template use File | Settings | File Templates.
 */
public class SendSMSTaskFactory extends ModelFactory<SendSMSTask> {
    @Override
    public SendSMSTask define() {
        SendSMSTask smsTask = new SendSMSTask();
        smsTask.taskNo = "123";
        smsTask.deleted= DeletedStatus.UN_DELETED;
        return smsTask;
    }

}
