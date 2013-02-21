package models.yihaodian;

import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-9-15
 */
@OnApplicationStart(async = true)
public class YHDGroupBuyJobConsumer extends RabbitMQConsumer<String> {
    public static String DATE_FORMAT = "yyy-MM-dd HH:mm:ss";

    @Override
    protected void consume(String orderId) {
        //开启事务管理
        JPAPlugin.startTx(false);

        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId",
                OuterOrderPartner.YHD, orderId).first();
        if(outerOrder == null || outerOrder.ybqOrder == null){
            Logger.info("can not find outerOrder: %s", orderId);
            JPAPlugin.closeTx(true);
            return;
        }
        if(outerOrder.status == OuterOrderStatus.ORDER_DONE){
            if(syncOrderDone(outerOrder)){
                outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
                outerOrder.save();
            }
        }

        boolean rollBack = false;
        try {
            JPA.em().flush();
        } catch (RuntimeException e) {
            rollBack = true;
            Logger.info("update yihaodian group buy order status failed, will roll back", e);
            //不抛异常 不让mq重试本job
        } finally {
            JPAPlugin.closeTx(rollBack);
        }
    }

    private boolean syncOrderDone(OuterOrder outerOrder) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Map<String, String> params = new HashMap<>();
        params.put("orderCode", outerOrder.orderId);
        params.put("partnerOrderCode", outerOrder.ybqOrder.orderNumber);
        params.put("orderAmount", outerOrder.ybqOrder.amount.toString());
        params.put("orderCreateTime", dateFormat.format(outerOrder.ybqOrder.createdAt));

        YHDResponse response = YHDUtil.sendRequest(params, "yhd.group.buy.order.verify", "//totalCount");
        return response.isOk();
    }

    @Override
    protected Class getMessageType() {
        return String.class;
    }

    @Override
    protected String queue() {
        return YihaodianQueueUtil.GROUP_BUY_QUEUE_NAME;
    }
}
