package models.yihaodian;

import models.order.Order;
import models.yihaodian.groupbuy.YHDGroupBuyOrder;
import models.yihaodian.groupbuy.YHDGroupBuyOrderJobFlag;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
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
        YHDGroupBuyOrder yhdGroupBuyOrder = YHDGroupBuyOrder.find("byOrderCode",message.getOrderCode()).first();
        if(yhdGroupBuyOrder == null){
            JPAPlugin.closeTx(true);
            return;
        }
        if(yhdGroupBuyOrder.jobFlag == YHDGroupBuyOrderJobFlag.ORDER_DONE){
            if(syncOrderDone(yhdGroupBuyOrder)){
                yhdGroupBuyOrder.jobFlag = YHDGroupBuyOrderJobFlag.ORDER_SYNCED;
                yhdGroupBuyOrder.save();
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

    private boolean syncOrderDone(YHDGroupBuyOrder yhdGroupBuyOrder) {
        Order ybqOrder = Order.findById(yhdGroupBuyOrder.ybqOrderId);
        if(ybqOrder == null){
            return false;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Map<String, String> params = new HashMap<>();
        params.put("orderCode", yhdGroupBuyOrder.orderCode);
        params.put("partnerOrderCode", ybqOrder.orderNumber);//测试公司
        params.put("orderAmount", ybqOrder.amount.toString());
        params.put("orderCreateTime", dateFormat.format(ybqOrder.createdAt));
        Logger.info("yhd.group.buy.order.verify orderCode %s", params.get("orderCode"));
        Logger.info("yhd.group.buy.order.verify partnerOrderCode %s", params.get("partnerOrderCode"));
        Logger.info("yhd.group.buy.order.verify orderAmount %s", params.get("orderAmount"));
        Logger.info("yhd.group.buy.order.verify orderCreateTime %s", params.get("orderCreateTime"));

        String responseXml = Util.sendRequest(params, "yhd.group.buy.order.verify");
        Logger.info("yhd.group.buy.order.verify response %s", responseXml);
        if (responseXml != null) {
            Response res = new Response();
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
