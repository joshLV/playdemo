package jobs.ktv;

import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.ktv.KtvOrderStatus;
import models.ktv.KtvRoomOrderInfo;
import models.order.Order;
import models.sales.Shop;
import models.sms.SMSMessage;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import play.jobs.On;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: yan
 * Date: 13-5-27
 * Time: 上午11:22
 */
@JobDefine(title = "给每个门店发送预订KTV信息", description = "每天9点执行，把当天的ktv预订信息发给每个门店")
@On("0 0 9 * * ?")
public class SendDailyScheduledMessage extends JobWithHistory {
    private static final String DECIMAL_FORMAT = "0000";

    @Override
    public void doJobWithHistory() {
        List<KtvRoomOrderInfo> roomOrderInfoList = KtvRoomOrderInfo.find("scheduledDay =? and status=? order by shop",
                DateUtils.truncate(new Date(), Calendar.DATE), KtvOrderStatus.DEAL).fetch();

        SimpleDateFormat dateFormat = new SimpleDateFormat(Order.COUPON_EXPIRE_FORMAT);
        DecimalFormat decimalFormat = new DecimalFormat(DECIMAL_FORMAT);
        Set<Shop> shopSet = new HashSet<>();
        List<Shop> shopList = new ArrayList<>();

        //查出有几个门店
        for (KtvRoomOrderInfo roomOrderInfo : roomOrderInfoList) {
            if (!shopSet.contains(roomOrderInfo.shop)) {
                shopList.add(roomOrderInfo.shop);
            }
            shopSet.add(roomOrderInfo.shop);
        }
        //遍历查出每个门店对应的预订信息，预订信息超过12个则换一个短信发送。每条短信长度最多300个
        int msgCount = 0;
        for (Shop shop : shopList) {
            Map<Shop, String> shopScheduledMap = new HashMap<>();
            List<Map<Shop, String>> scheduledList = new ArrayList<>();
            StringBuilder scheduleInfo = new StringBuilder();
            String code = decimalFormat.format(msgCount + 1000);
            for (KtvRoomOrderInfo orderInfo : roomOrderInfoList) {
                String info = shopScheduledMap.get(orderInfo.shop);
                if (!orderInfo.shop.id.equals(shop.id)) {
                    continue;
                }
                if (StringUtils.isBlank(info)) {
                    shopScheduledMap.put(shop, appendInfo(scheduleInfo, dateFormat, orderInfo, shop));
                } else {
                    shopScheduledMap.put(shop, appendInfo(scheduleInfo, orderInfo));
                }
                scheduledList.add(shopScheduledMap);
                if (scheduledList.size() >= 12) {
                    sendSmsMQ(shopScheduledMap.get(shop), shop, code);
                    shopScheduledMap = new HashMap<>();
                    scheduledList = new ArrayList<>();
                    scheduleInfo = new StringBuilder();
                }
            }
            msgCount++;
            sendSmsMQ(shopScheduledMap.get(shop), shop, code);
        }
    }

    private String appendInfo(StringBuilder builder, KtvRoomOrderInfo orderInfo) {
        builder.append("【");
        builder.append(orderInfo.orderItem.phone);
        builder.append(orderInfo.roomType.getName());
        builder.append("（");
        builder.append(orderInfo.orderItem.buyNumber);
        builder.append("间）");
        builder.append(getKtvScheduleTime(orderInfo.scheduledTime, orderInfo.product.duration));
        builder.append("】");
        return builder.toString();
    }

    private String appendInfo(StringBuilder builder, SimpleDateFormat dateFormat, KtvRoomOrderInfo orderInfo, Shop shop) {
        builder.append(dateFormat.format(orderInfo.scheduledDay));
        builder.append(shop.name);
        builder.append("预订【");
        builder.append(orderInfo.orderItem.phone);
        builder.append(orderInfo.roomType.getName());
        builder.append("（");
        builder.append(orderInfo.orderItem.buyNumber);
        builder.append("间）");
        builder.append(getKtvScheduleTime(orderInfo.scheduledTime, orderInfo.product.duration));
        builder.append("】");
        return builder.toString();
    }

    private void sendSmsMQ(String content, Shop shop, String code) {
        if (shop == null || StringUtils.isBlank(shop.managerMobiles)) {
            return;
        }
        String[] mobileArray = StringUtils.trimToEmpty(shop.managerMobiles).split(",");
        for (String mobile : mobileArray) {
            new SMSMessage(content, mobile, code).send();
        }
    }

    private String getKtvScheduleTime(int scheduledTime, int duration) {
        return scheduledTime + "点至" + (scheduledTime + duration) + "点";
    }
}
