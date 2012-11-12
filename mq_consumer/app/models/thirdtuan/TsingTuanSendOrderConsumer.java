package models.thirdtuan;

import java.util.HashMap;
import java.util.Map;

import models.RabbitMQConsumerWithTx;
import models.tsingtuan.TsingTuanOrder;
import models.tsingtuan.TsingTuanSendOrder;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import play.jobs.OnApplicationStart;
import util.ws.WebServiceCallback;
import util.ws.WebServiceClient;
import util.ws.WebServiceClientFactory;

@OnApplicationStart(async = true)
public class TsingTuanSendOrderConsumer extends RabbitMQConsumerWithTx<TsingTuanOrder> {

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
        return TsingTuanSendOrder.SEND_ORDER;
    }

    public static final String SEND_URL = "http://www.tsingtuan.com/outer/shihui/order.php";

    
    public void sendOrder(TsingTuanOrder order) {
        //准备url
        Map<String, Object> params = new HashMap<>();

        params.put("order_id", order.orderId.toString());
        params.put("team_id", order.teamId.toString());
        params.put("state", order.state);
        params.put("fare", order.fare.toString());
        params.put("money", order.money.toString());
        params.put("origin", order.origin.toString());
        params.put("address", order.address);
        params.put("zipcode", order.zipcode);
        params.put("realname", order.realname);
        params.put("mobile", order.mobile);
        params.put("quantity", order.quantity.toString());
        params.put("remark", order.remark);
        params.put("condbuy", order.condbuy);
        params.put("create_time", order.create_time.toString());
        params.put("pay_time", order.pay_time.toString());
        params.put("coupons", order.coupons + "," + order.password);
        params.put("sign", order.getSign());
        
        WebServiceClient client = WebServiceClientFactory.getClientHelper();
        client.postString("TsingTuanSendOrder", SEND_URL, params, order.orderId.toString(), order.teamId.toString(), new WebServiceCallback() {
            @Override
            public void process(int statusCode, String returnContent) {
                System.out.println("statusCode=" + statusCode);
                System.out.println("returnContent=" + returnContent);
            }
        });

    }
}
