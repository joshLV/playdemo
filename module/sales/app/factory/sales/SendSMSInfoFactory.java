package factory.sales;

import com.uhuila.common.constants.DeletedStatus;
import factory.ModelFactory;
import models.sales.SendSMSInfo;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-26
 * Time: 下午5:19
 * To change this template use File | Settings | File Templates.
 */
public class SendSMSInfoFactory extends ModelFactory<SendSMSInfo> {
    @Override
    public SendSMSInfo define() {
        SendSMSInfo smsInfo = new SendSMSInfo();
        smsInfo.taskNo="123";
        smsInfo.mobile="13901894562";
        smsInfo.eCouponSn="123456";
        smsInfo.text="1234";
        smsInfo.sendAt=new Date();
        smsInfo.createdAt=new Date();
        smsInfo.deleted= DeletedStatus.UN_DELETED;
        smsInfo.lockVersion=0;
        return smsInfo;
    }
}
