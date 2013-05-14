package models.job.yihaodian.listener;

import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.resale.Resaler;
import models.yihaodian.YHDResponse;
import models.yihaodian.YHDUtil;
import org.w3c.dom.Node;
import play.Logger;
import play.Play;
import play.jobs.Every;
import play.libs.XPath;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         <p/>
 *         拉取新的订单，与本地比较，若从未记录，则记录下来
 *         <p/>
 *         Date: 12-8-29
 */
@JobDefine(title = "一号店订单记录", description = "拉取新的订单，与本地比较，若从未记录，则记录下来")
@Every("1mn")
public class OrderListener extends JobWithHistory {
    private static String ORDER_DATE = "yyyy-MM-dd HH:mm:ss";
    public static final boolean ON = Play.configuration.getProperty("yihaodian.listener", "off").toLowerCase().equals("on");

    @Override
    public void doJobWithHistory() {
        if (!ON && !Play.runingInTestMode()) {
            Logger.info("yihaodian order listener aborted");
            return;
        }
        //从一号店拉取订单列表
        List<Node> orders = newOrders();
        if (orders == null || orders.size() == 0) {
            return;
        }
        //筛选出我们没有处理过的
        for (Node order : orders) {
            String orderCode = XPath.selectText("./orderCode", order).trim();
            if (OuterOrder.find("byOrderIdAndPartner", orderCode, OuterOrderPartner.YHD).first() == null) {
                OuterOrder outerOrder = new OuterOrder();
                //此处不保存outerOrder的message，等处理的时候会再去一号店拉取最新的订单信息并保存
                outerOrder.status = OuterOrderStatus.ORDER_COPY;
                outerOrder.partner = OuterOrderPartner.YHD;
                outerOrder.resaler = Resaler.findApprovedByLoginName(Resaler.YHD_LOGIN_NAME);
                outerOrder.orderId = orderCode;
                outerOrder.save();
            }
        }
    }

    /**
     * http://openapi.yihaodian.com/forward/inshop/yhd.orders.get.html
     *
     * @return 已付款未发货的订单摘要
     */
    public List<Node> newOrders() {
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

        if (response.isOk()) {
            return XPath.selectNodes("./order", response.data);
        }
        return null;
    }
}
