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
        qparams.add(new BasicNameValuePair("coupons", order.coupons));
        qparams.add(new BasicNameValuePair("sign", order.getRefundSign()));
        
        String url = SEND_URL + URLEncodedUtils.format(qparams, "UTF-8");
        
        getHttpClientHelper().processGetUrl(url, new HttpCallback() {
            @Override
            public void process(int statusCode, String returnContent) {
                System.out.println("statusCode=" + statusCode);
                System.out.println("returnContent=" + returnContent);
            }
        });

    }
}
