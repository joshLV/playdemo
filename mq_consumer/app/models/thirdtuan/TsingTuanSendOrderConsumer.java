package models.thirdtuan;

import java.util.ArrayList;
import java.util.List;

import models.tsingtuan.TsingTuanOrder;
import models.tsingtuan.TsingTuanSendOrder;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import play.modules.rabbitmq.consumer.RabbitMQConsumer;
import util.http.DefaultHttpClientHelper;
import util.http.HttpCallback;
import util.http.HttpClientHelper;

public class TsingTuanSendOrderConsumer extends RabbitMQConsumer<TsingTuanOrder> {

    @Override
    protected void consume(TsingTuanOrder order) {
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

    public static final String SEND_URL = "http://www.tsingtuan.com/outer/shihui/order.php?";

    private HttpClientHelper httpClientHelper;

    public HttpClientHelper getHttpClientHelper() {
        if (httpClientHelper != null) {
            return httpClientHelper;
        }
        return DefaultHttpClientHelper.getInstance();
    }

    public void setHttpClientHelper(HttpClientHelper httpClientHelper) {
        this.httpClientHelper = httpClientHelper;
    }
    
    public void sendOrder(TsingTuanOrder order) {
        //准备url
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();

        qparams.add(new BasicNameValuePair("order_id", order.orderId.toString()));
        qparams.add(new BasicNameValuePair("team_id", order.teamId.toString()));
        qparams.add(new BasicNameValuePair("state", order.state));
        qparams.add(new BasicNameValuePair("fare", order.fare.toString()));
        qparams.add(new BasicNameValuePair("money", order.money.toString()));
        qparams.add(new BasicNameValuePair("origin", order.origin.toString()));
        qparams.add(new BasicNameValuePair("address", order.address));
        qparams.add(new BasicNameValuePair("zipcode", order.zipcode));
        qparams.add(new BasicNameValuePair("realname", order.realname));
        qparams.add(new BasicNameValuePair("mobile", order.mobile));
        qparams.add(new BasicNameValuePair("quantity", order.quantity.toString()));
        qparams.add(new BasicNameValuePair("remark", order.remark));
        qparams.add(new BasicNameValuePair("condbuy", order.condbuy));
        qparams.add(new BasicNameValuePair("create_time", order.create_time.toString()));
        qparams.add(new BasicNameValuePair("pay_time", order.pay_time.toString()));
        qparams.add(new BasicNameValuePair("coupons", order.coupons + "," + order.password));
        qparams.add(new BasicNameValuePair("sign", order.getSign()));
        
        String url = SEND_URL + URLEncodedUtils.format(qparams, "UTF-8");
        
        System.out.println("tsingtuan url=" + url);
        
        getHttpClientHelper().processGetUrl(url, new HttpCallback() {
            @Override
            public void process(int statusCode, String returnContent) {
                System.out.println("statusCode=" + statusCode);
                System.out.println("returnContent=" + returnContent);
            }
        });

    }
}
