package models.yihaodian;

import play.Logger;
import play.libs.WS;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

/**
 * @author likang
 */
public class YihaodianJobConsumer extends RabbitMQConsumer<YihaodianJobMessage>{
    @Override
    protected void consume(YihaodianJobMessage message) {
        YihaodianOrder yihaodianOrder = YihaodianOrder.findById(message.getYihaodianOrderId());
        if(yihaodianOrder == null){
            Logger.error("order not found: %s", message.getYihaodianOrderId());
            return;
        }

        String actions = yihaodianOrder.pendingActions;
        if(actions == null || actions.trim().equals("")){
            return;
        }

        int commaIndex = yihaodianOrder.pendingActions.indexOf(",");
        int nextIndex = commaIndex + 1;
        if(commaIndex == -1){
            commaIndex = yihaodianOrder.pendingActions.length();
            nextIndex = commaIndex;
        }
        String action = yihaodianOrder.pendingActions.substring(0, commaIndex);

        switch (action){
            case "mark_send":
                markSend(yihaodianOrder);
                break;
            case "mark_consumed":
                markConsumed(yihaodianOrder);
                break;
            case "mark_refunded":
                markRefunded(yihaodianOrder);
                break;
            default:
                Logger.error("unknown action: %s of order: %s", action, message.getYihaodianOrderId());
        }

        yihaodianOrder.pendingActions = yihaodianOrder.pendingActions.substring(nextIndex);
        yihaodianOrder.save();
    }

    private void markSend(YihaodianOrder yihaodianOrder){
        WS.WSRequest request = WS.url("http://yihaodian.com");
        //todo
        //request.setParameter("a", "a");
        WS.HttpResponse response = request.get();
    }

    private void markConsumed(YihaodianOrder yihaodianOrder){
        WS.WSRequest request = WS.url("http://yihaodian.com");
        //todo
        //request.setParameter("a", "a");
        WS.HttpResponse response = request.get();
    }

    private void markRefunded(YihaodianOrder yihaodianOrder){
        WS.WSRequest request = WS.url("http://yihaodian.com");
        //todo
        //request.setParameter("a", "a");
        WS.HttpResponse response = request.get();
    }

    @Override
    protected Class getMessageType() {
        return YihaodianJobMessage.class;
    }

    @Override
    protected String queue() {
        return YihaodianJobUtil.QUEUE_NAME;
    }
}
