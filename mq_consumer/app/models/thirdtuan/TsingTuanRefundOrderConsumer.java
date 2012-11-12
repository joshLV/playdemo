package models.thirdtuan;

import java.util.HashMap;
import java.util.Map;

import models.RabbitMQConsumerWithTx;
import models.tsingtuan.TsingTuanOrder;
import models.tsingtuan.TsingTuanSendOrder;
import play.jobs.OnApplicationStart;
import util.ws.WebServiceCallback;
import util.ws.WebServiceClient;
import util.ws.WebServiceClientFactory;

@OnApplicationStart(async = true)
public class TsingTuanRefundOrderConsumer extends RabbitMQConsumerWithTx<TsingTuanOrder> {

    @Override
    public void consumeWithTx(TsingTuanOrder order) {
        sendOrder(order);
    }

    @Override
    protected Class getMessageType() {
        return TsingTuanOrder.class;
    }

    @Override
    protected String queue() {
        return TsingTuanSendOrder.REFUND_ORDER;
    }

    public static final String SEND_URL = "http://www.tsingtuan.com/outer/shihui/refund.php?";
    
    public void sendOrder(TsingTuanOrder order) {
        //准备url
        Map<String, Object> params = new HashMap<>();

        params.put("order_id", order.orderId.toString());
        params.put("team_id", order.teamId.toString());
        params.put("refund_time", order.refund_time.toString());
        params.put("coupons", order.coupons);
        params.put("sign", order.getRefundSign());
        
        WebServiceClient client = WebServiceClientFactory.getClientHelper();
        client.postString("TsingTuanRefundOrder", SEND_URL, params, order.orderId.toString(), order.teamId.toString(), new WebServiceCallback() {
            @Override
            public void process(int statusCode, String returnContent) {
                System.out.println("refund.statusCode=" + statusCode);
                System.out.println("refund.returnContent=" + returnContent);
            }
        });

    }
}
