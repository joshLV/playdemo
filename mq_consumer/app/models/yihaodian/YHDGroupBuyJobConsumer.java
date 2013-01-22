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
public class YHDGroupBuyJobConsumer extends RabbitMQConsumer<YHDGroupBuyMessage> {
    public static String DATE_FORMAT = "yyy-MM-dd HH:mm:ss";

    @Override
    protected void consume(YHDGroupBuyMessage message) {
        //开启事务管理
        JPAPlugin.startTx(false);

        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId",
                OuterOrderPartner.YHD,message.getOrderId()).first();
        if(outerOrder == null || outerOrder.ybqOrder == null){
            Logger.info("can not find outerOrder: %s", message.getOrderId());
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
        params.put("orderCode", String.valueOf(outerOrder.orderId));
        params.put("partnerOrderCode", outerOrder.ybqOrder.orderNumber);
        params.put("orderAmount", outerOrder.ybqOrder.amount.toString());
        params.put("orderCreateTime", dateFormat.format(outerOrder.ybqOrder.createdAt));
        Logger.info("yhd.group.buy.order.verify orderCode %s", params.get("orderCode"));
        Logger.info("yhd.group.buy.order.verify partnerOrderCode %s", params.get("partnerOrderCode"));
        Logger.info("yhd.group.buy.order.verify orderAmount %s", params.get("orderAmount"));
        Logger.info("yhd.group.buy.order.verify orderCreateTime %s", params.get("orderCreateTime"));

        String responseXml = YHDUtil.sendRequest(params, "yhd.group.buy.order.verify");
        Logger.info("yhd.group.buy.order.verify response %s", responseXml);
        if (responseXml != null) {
            YHDResponse res = new YHDResponse();
            res.parseXml(responseXml, null, false, null);
            if(res.getErrorCount() == 0){
                return true;
            }else {
                Logger.info("yhd.group.buy.order.verify error");
            }
        }
        return false;
    }

    @Override
    protected Class getMessageType() {
        return YHDGroupBuyMessage.class;
    }

    @Override
    protected String queue() {
        return YihaodianQueueUtil.GROUP_BUY_QUEUE_NAME;
    }
}
