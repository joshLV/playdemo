package jobs.baidu;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import models.baidu.BaiduResponse;
import models.baidu.BaiduUtil;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.resale.Resaler;
import play.Logger;
import play.Play;
import play.jobs.Every;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: yan
 * Date: 13-7-12
 * Time: 下午8:52
 */
@JobDefine(title = "百度订单记录", description = "拉取新的订单，与本地比较，若从未记录，则记录下来")
//@Every("1mn")
public class BaiduOrderScanner extends JobWithHistory {
    private static String ORDER_DATE = "yyyy-MM-dd HH:mm:ss";

    @Override
    public void doJobWithHistory() {
        if (!Play.runingInTestMode()) {
            Logger.info("baidu order scanner aborted");
            return;
        }
        //从百度拉取订单列表
        JsonArray orders = newOrders();
        if (orders == null || orders.size() == 0) {
            return;
        }
        //筛选出我们没有处理过的
        for (JsonElement order : orders) {
            JsonObject orderObject = order.getAsJsonObject();
            String orderId = orderObject.get("order_id").getAsString();
            if (OuterOrder.getOuterOrder(orderId, OuterOrderPartner.BD) == null) {
                OuterOrder outerOrder = new OuterOrder();
                //此处不保存outerOrder的message，等处理的时候会再去百度取最新的订单信息并保存
                outerOrder.status = OuterOrderStatus.ORDER_COPY;
                outerOrder.partner = OuterOrderPartner.BD;
                outerOrder.resaler = Resaler.findApprovedByLoginName(Resaler.BAIDU_LOGIN_NAME);
                outerOrder.orderId = orderId;
                outerOrder.save();
            }
        }
    }

    private JsonArray newOrders() {
        Date end = new Date(System.currentTimeMillis() + 600000);//当前时间往后10分钟
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(end);
        calendar.add(Calendar.DAY_OF_MONTH, -14);
        Date start = calendar.getTime();

        Map<String, Object> params = new HashMap<>();
//        params.put("status", "2");//已付款订单
        params.put("from", new SimpleDateFormat(ORDER_DATE).format(start));
        params.put("to", new SimpleDateFormat(ORDER_DATE).format(end));
        BaiduResponse response = BaiduUtil.sendRequest(params, "getOrderByTime.action");
        return response.data.getAsJsonArray();
    }
}
