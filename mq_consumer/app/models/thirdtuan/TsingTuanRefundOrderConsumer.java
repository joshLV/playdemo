package models.thirdtuan;

import java.util.ArrayList;
import java.util.List;

import models.tsingtuan.TsingTuanOrder;
import models.tsingtuan.TsingTuanSendOrder;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import play.modules.rabbitmq.consumer.RabbitMQConsumer;
import util.ws.WebServiceCallback;
import util.ws.WebServiceClientFactory;
import util.ws.WebServiceClientHelper;

public class TsingTuanRefundOrderConsumer extends RabbitMQConsumer<TsingTuanOrder> {

    @Override
    protected void consume(TsingTuanOrder arg0) {
        
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
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();

        qparams.add(new BasicNameValuePair("order_id", order.orderId.toString()));
        qparams.add(new BasicNameValuePair("team_id", order.teamId.toString()));
        qparams.add(new BasicNameValuePair("coupons", order.coupons));
        qparams.add(new BasicNameValuePair("sign", order.getRefundSign()));
        
        String url = SEND_URL + URLEncodedUtils.format(qparams, "UTF-8");

        WebServiceClientHelper client = WebServiceClientFactory.getClientHelper();
        client.getString("TsingTuanRefundOrder", url, order.orderId.toString(), order.teamId.toString(), new WebServiceCallback() {
            @Override
            public void process(int statusCode, String returnContent) {
                System.out.println("statusCode=" + statusCode);
                System.out.println("returnContent=" + returnContent);
            }
        });

    }
}
