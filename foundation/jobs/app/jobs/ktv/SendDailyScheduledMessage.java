package jobs.ktv;

import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.ktv.KtvOrderStatus;
import models.ktv.KtvRoomOrderInfo;
import org.apache.commons.lang.time.DateUtils;
import play.jobs.On;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: yan
 * Date: 13-5-27
 * Time: 上午11:22
 */
@JobDefine(title = "给每个门店发送预订KTV信息", description = "每天9点执行，把当天的ktv预订信息发给每个门店")
//@On("0 0 9 * * ?")
public class SendDailyScheduledMessage extends JobWithHistory {
    @Override
    public void doJobWithHistory() {
        List<KtvRoomOrderInfo> roomOrderInfoList = KtvRoomOrderInfo.find("scheduledDay =? and status=?",
                DateUtils.truncate(new Date(), Calendar.DATE), KtvOrderStatus.DEAL).fetch();
        StringBuilder builder = new StringBuilder();

        for (KtvRoomOrderInfo orderInfo : roomOrderInfoList) {
            builder.append(orderInfo.shop.name);
            builder.append("预订【");
            builder.append(orderInfo.orderItem.phone);
            builder.append(orderInfo.roomType.getName());
            builder.append(orderInfo.scheduledTime);
            builder.append("】");
        }
    }
}
