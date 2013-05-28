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
import play.data.validation.Validation;
import play.jobs.On;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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


        for (KtvRoomOrderInfo roomOrderInfo : roomOrderInfoList) {
            shopSet.add(roomOrderInfo.shop);
        }
        Map<Shop, String> shopScheduledMap = new HashMap<>();
        String info = "";
        int msgCount = 0;
        System.out.println(shopSet.size());
        for (Shop shop : shopSet) {
            List<Map<Shop, String>> scheduledList = new ArrayList<>();
            StringBuilder scheduleInfo = new StringBuilder();
            String code = decimalFormat.format(msgCount + 1000);
            for (KtvRoomOrderInfo orderInfo : roomOrderInfoList) {
                info = shopScheduledMap.get(orderInfo.shop);
                if (!orderInfo.shop.id.equals(shop.id)) {
                    continue;
                }
                System.out.println(info + ">>>>"+shop);
                if (StringUtils.isBlank(info)) {
                    shopScheduledMap.put(shop, appendInfo(scheduleInfo, dateFormat, orderInfo, shop));
                } else {
                    shopScheduledMap.put(shop, appendInfo(scheduleInfo, orderInfo));
                }
                scheduledList.add(shopScheduledMap);
                System.out.println(scheduledList.size() + "---" + shopScheduledMap.get(shop));
            }
            msgCount++;
            if (scheduledList.size() > 3) {
                shopScheduledMap = new HashMap<>();
            } else {
                sendSmsMQ(shopScheduledMap.get(shop), shop, code);
            }
        }


//        StringBuilder builder = null;
//        int msgCount = 0;
//        int sameShopCount = 1;
//
//        Long preShopId = null;
//        for (KtvRoomOrderInfo orderInfo : roomOrderInfoList) {
//            Shop shop = orderInfo.shop;
//            String code = decimalFormat.format(msgCount + 1000);
//            if (msgCount > 0 && !shop.id.equals(preShopId)) {
//                sendSmsMQ(builder, shop, code);
//                builder = new StringBuilder();
//            }
//            if (preShopId == null || msgCount == 0 || !shop.id.equals(preShopId)) {
//                builder = getStringBuilder(dateFormat, orderInfo, shop);
//            }
//
//            //每次都是下次执行是否发送
//            if (msgCount > 0) {
//                if (shop.id.equals(preShopId)) {
//                    appendInfo(builder, orderInfo);
//                    sameShopCount++;
//                } else {
//                    sendSmsMQ(builder, shop, code);
//                    builder = new StringBuilder();
//                }
//
//                //超过12个则换一条短信发送
//                if (sameShopCount == 12) {
//                    sendSmsMQ(builder, shop, code);
//                    sameShopCount = 1;
//                    builder = getStringBuilder(dateFormat, orderInfo, shop);
//                }
//            }
//            if (roomOrderInfoList.size() == 1 || (roomOrderInfoList.size() == (msgCount + 1))) {
//                sendSmsMQ(builder, shop, code);
//                builder = new StringBuilder();
//            }
//
//            preShopId = shop.id;
//            msgCount++;
//        }

    }

    private String appendInfo(StringBuilder builder, KtvRoomOrderInfo orderInfo) {
        builder.append("【");
        builder.append(orderInfo.orderItem.phone);
        builder.append(orderInfo.roomType.getName());
        builder.append("(");
        builder.append(orderInfo.orderItem.buyNumber);
        builder.append("间)");
        builder.append(getKtvScheduleTime(orderInfo.scheduledTime, orderInfo.duration));
        builder.append("】");
        return builder.toString();
    }

    private String appendInfo(StringBuilder builder, SimpleDateFormat dateFormat, KtvRoomOrderInfo orderInfo, Shop shop) {
        builder.append(dateFormat.format(orderInfo.scheduledDay));
        builder.append(shop.name);
        builder.append("预订【");
        builder.append(orderInfo.orderItem.phone);
        builder.append(orderInfo.roomType.getName());
        builder.append("(");
        builder.append(orderInfo.orderItem.buyNumber);
        builder.append("间)");
        builder.append(getKtvScheduleTime(orderInfo.scheduledTime, orderInfo.duration));
        builder.append("】");
        return builder.toString();
    }

    private void sendSmsMQ(String content, Shop shop, String code) {
        if (shop.managerMobiles.length() == 0) {
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
