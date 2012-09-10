package models.job.yihaodian.listener;

import models.yihaodian.YihaodianOrder;
import models.yihaodian.OrderItem;
import models.yihaodian.Response;
import models.yihaodian.Util;
import models.yihaodian.YihaodianJobMessage;
import models.yihaodian.YihaodianQueueUtil;
import org.dom4j.DocumentException;
import play.Logger;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author likang
 *
 * 拉取新的订单，与本地比较，若从未记录，则记录下来，并发起一个处理请求放队列里
 *
 * Date: 12-8-29
 */
@Every("1mn")
public class OrderListener extends Job{
    private static String ORDER_DATE = "yyyy-MM-dd HH:mm:ss";
    public static final boolean ON = Play.configuration.getProperty("yihaodian.listener", "off").toLowerCase().equals("on");

    @Override
    public void doJob(){
        if (!ON){
            return;
        }
        Logger.info("start yihaodian job");
        //从一号店拉取订单列表
        List<YihaodianOrder> orders = newOrders();
        if (orders != null && orders.size() > 0) {
            //筛选出我们没有处理过的
            StringBuilder orderCodes = new StringBuilder();
            for(YihaodianOrder order : orders){
                if(YihaodianOrder.find("byOrderId", order.orderId).first() == null){
                    order.save();
                    orderCodes.append(order.orderCode).append(",");
                }else {
                    //发送消息队列
                    YihaodianJobMessage message = new YihaodianJobMessage(order.orderId);
                    YihaodianQueueUtil.addJob(message);
                }
            }

            //拉取订单的全部信息
            List<YihaodianOrder> fullOrders = fullOrders(orderCodes.toString());
            if(fullOrders != null && fullOrders.size() > 0) {
                for(YihaodianOrder order: fullOrders) {
                    order.save();
                    for(OrderItem orderItem : order.orderItems) {
                        orderItem.order = order;
                        orderItem.save();
                    }
                    //发送消息队列
                    YihaodianJobMessage message = new YihaodianJobMessage(order.orderId);
                    YihaodianQueueUtil.addJob(message);
                }
            }
        }
    }

    /**
     * http://openapi.yihaodian.com/forward/inshop/yhd.orders.get.html
     *
     * @return 已付款未发货的订单摘要
     */
    public List<YihaodianOrder> newOrders(){
        Date end = new Date(System.currentTimeMillis() + 600000);//当前时间往后10分钟
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(end);
        calendar.add(Calendar.DAY_OF_MONTH, -14);
        Date start = calendar.getTime();


        Map<String, String> params = new HashMap<>();
        params.put("orderStatusList", "ORDER_WAIT_SEND");//按已付款的状态查询
        params.put("dateType", "1");//按付款时间查询
        params.put("startTime", new SimpleDateFormat(ORDER_DATE).format(start));
        params.put("endTime", new SimpleDateFormat(ORDER_DATE).format(end));
        Logger.info("yhd.orders.get orderStatusList %s", params.get("orderStatusList"));
        Logger.info("yhd.orders.get dateType %s", params.get("dateType"));
        Logger.info("yhd.orders.get startTime %s", params.get("startTime"));
        Logger.info("yhd.orders.get endTime %s", params.get("endTime"));

        String responseXml = Util.sendRequest(params, "yhd.orders.get");

        Logger.info("yhd.orders.get response %s", responseXml);
        if(responseXml != null) {
            Response<YihaodianOrder> res = new Response<>();
            res.parseXml(responseXml, "orderList", true, YihaodianOrder.parser);
            if(res.getErrorCount() == 0){
                return res.getVs();
            }
        }
        return null;
    }

    /**
     * 订单的详细信息
     * http://openapi.yihaodian.com/forward/inshop/yhd.orders.detail.get.html
     *
     * @param orderCodes 订单编号列表，以逗号分隔
     * @return 订单的详细信息
     */
    public List<YihaodianOrder> fullOrders(String orderCodes){
        Map<String, String> params = new HashMap<>();
        params.put("orderCodeList", orderCodes);

        String responseXml = Util.sendRequest(params, "yhd.orders.detail.get");
        Logger.info("yhd.orders.detail.get: %s", responseXml);
        if (responseXml != null) {
            Response<YihaodianOrder> res = new Response<>();
            res.parseXml(responseXml, "orderInfoList", true, YihaodianOrder.fullParser);
            if(res.getErrorCount() == 0){
                return res.getVs();
            }
        }
        return null;
    }
}
