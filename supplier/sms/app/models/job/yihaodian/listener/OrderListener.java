package models.job.yihaodian.listener;

import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.yihaodian.*;
import models.yihaodian.YHDUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.libs.XPath;

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
    public static final boolean ON = Play.configuration.getProperty("yihaodian.listener", "on").toLowerCase().equals("on");

    @Override
    public void doJob(){
        if (!ON){
            return;
        }
        //从一号店拉取订单列表
        NodeList orders = newOrders();
        if (orders == null || orders.getLength() == 0) {
            return;
        }
        //筛选出我们没有处理过的
        StringBuilder orderCodes = new StringBuilder();
        for(int i = 0; i < orders.getLength(); i++ ){
            Node order = orders.item(i);
            Long orderId = Long.parseLong(XPath.selectText("//orderId", order));
            String orderCode = XPath.selectText("//orderCode", order);

            if(OuterOrder.find("byOrderId", orderId).first() == null){
                orderCodes.append(orderCode).append(",");
            }else {
                //发送消息队列
                YihaodianJobMessage message = new YihaodianJobMessage(orderId);
                YihaodianQueueUtil.addJob(message);
            }
        }

        //保存新订单以待处理
        if(orderCodes.length() > 0){
            Map<String, String> params = new HashMap<>();
            params.put("orderCodeList", orderCodes.toString());
            YHDResponse response = YHDUtil.sendRequest(params, "yhd.orders.detail.get", "orderInfoList");

            if (response.isOk()) {
                NodeList fullOrders = response.data.getChildNodes();
                for(int i =0; i< fullOrders.getLength(); i++) {
                    Node fullOrder = fullOrders.item(i);

                    OuterOrder outerOrder = new OuterOrder();
                    outerOrder.message = fullOrder.toString();
                    outerOrder.status = OuterOrderStatus.ORDER_COPY;
                    outerOrder.partner = OuterOrderPartner.YHD;
                    outerOrder.orderId = Long.parseLong(XPath.selectText("//orderDetail//orderId", fullOrder));
                    outerOrder.save();

                    //发送消息队列
                    YihaodianJobMessage message = new YihaodianJobMessage(outerOrder.orderId);
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
    public NodeList newOrders(){
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

        YHDResponse response = YHDUtil.sendRequest(params, "yhd.orders.get", "orderList");

        if(response.isOk()) {
            return response.data.getChildNodes();
        }
        return null;
    }
}
