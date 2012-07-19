package models.yihaodian;

import play.Play;
import play.modules.rabbitmq.producer.RabbitMQPublisher;

/**
 * @author likang
 */
public class YihaodianJobUtil {
    public static final String QUEUE_NAME = Play.mode.isProd() ? "yihaodian_job" : "yihaodian_job_dev";

    private YihaodianJobUtil(){}

    public static void addJob(Long yihaodianOrderId){
        RabbitMQPublisher.publish(QUEUE_NAME, new YihaodianJobMessage(yihaodianOrderId));
    }

    public static void addMarkConsumedJob(Long seewiOrderId){
        YihaodianOrder yihaodianOrder = appendOrderAction(seewiOrderId, "mark_consumed");
        if(yihaodianOrder == null){
            return;
        }
        addJob(yihaodianOrder.getId());
    }

    public static void addMarkRefundedJob(Long seewiOrderId){
        YihaodianOrder yihaodianOrder = appendOrderAction(seewiOrderId, "mark_refunded");
        if(yihaodianOrder == null){
            return;
        }
        addJob(yihaodianOrder.getId());
    }

    private static YihaodianOrder appendOrderAction(Long seewiOrderId, String action){
        YihaodianOrder yihaodianOrder = YihaodianOrder.find("bySeewiOrderId", seewiOrderId).first();
        if(yihaodianOrder == null){
            return null;
        }
        String curAction = yihaodianOrder.pendingActions == null ? "" : yihaodianOrder.pendingActions;
        yihaodianOrder.pendingActions = curAction + action + ",";
        return yihaodianOrder.save();
    }
}
