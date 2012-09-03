package models.job.yihaodian.listener;

import models.job.yihaodian.Order;
import models.job.yihaodian.OrderItem;
import models.job.yihaodian.Response;
import models.job.yihaodian.Util;
import models.yihaodian.YihaodianJobMessage;
import models.yihaodian.YihaodianQueueUtil;
import org.dom4j.DocumentException;
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
public class OrderListener extends Job{
    private static String ORDER_DATE = "yyyy-MM-dd HH:mm:ss";

    @Override
    public void doJob(){
        //从一号店拉取订单列表
        List<Order> orders = newOrders();
        if (orders != null && orders.size() > 0) {
            //筛选出我们没有处理过的
            StringBuilder orderCodes = new StringBuilder();
            for(Order order : orders){
                if(Order.find("byOrderId", order.orderId).first() == null){
                    orderCodes.append(order.orderCode).append(",");
                }
            }

            //拉取订单的全部信息
            List<Order> fullOrders = fullOrders(orderCodes.toString());
            if(fullOrders != null && fullOrders.size() > 0) {
                for(Order order: fullOrders) {
                    order.save();
                    for(OrderItem orderItem : order.orderItems) {
                        orderItem.order = order;
                        orderItem.save();
                    }
                    //发送消息队列
                    YihaodianJobMessage message = new YihaodianJobMessage(order.getId());
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
    public List<Order> newOrders(){
        Date end = new Date(System.currentTimeMillis() + 600000);//当前时间往后10分钟
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(end);
        calendar.add(Calendar.DAY_OF_MONTH, -14);
        Date start = calendar.getTime();


        Map<String, String> params = new HashMap<>();
        params.put("orderStatusList", "ORDER_WAIT_SEND");//按已付款的状态查询
        params.put("dateType", "2");//按付款时间查询
        params.put("startTime", new SimpleDateFormat(ORDER_DATE).format(start));
        params.put("endTime", new SimpleDateFormat(ORDER_DATE).format(end));
        String responseXml = Util.sendRequest(params, "yhd.orders.get");
        if(responseXml != null) {
            Response<Order> res = new Response<>();
            try {
                res.parseXml(responseXml, "orderList", true, Order.parser);
                return res.getVs();
            } catch (DocumentException e) {
                //
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
    public List<Order> fullOrders(String orderCodes){
        Map<String, String> params = new HashMap<>();
        params.put("orderCodeList", orderCodes);

        String responseXml = Util.sendRequest(params, "yhd.orders.detail.get");
        if (responseXml == null) {
            Response<Order> res = new Response<>();
            try{
                res.parseXml(responseXml, "orderInfoList", true, Order.fullParser);
                return res.getVs();
            } catch (DocumentException e) {
                //
            }
        }
        return null;
    }
}
