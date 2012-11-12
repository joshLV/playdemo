package models.thirdtuan;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.RabbitMQConsumerWithTx;
import models.tsingtuan.TsingTuanOrder;
import models.tsingtuan.TsingTuanSendOrder;
import play.Logger;
import play.jobs.OnApplicationStart;
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

    @Override
    protected int retries() {
        return 10;
    }

    @Override
    protected String routingKey() {
        return this.queue();
    }

    public static final String SEND_URL = "http://www.tsingtuan.com/outer/shihui/refund.php";

    private final Pattern RESULTCODE_PATTERN = Pattern.compile("^0\\|");
    
    public void sendOrder(TsingTuanOrder order) {
        //准备url
        Map<String, Object> params = new HashMap<>();

        params.put("order_id", order.orderId.toString());
        params.put("team_id", order.teamId.toString());
        params.put("refund_time", order.refund_time.toString());
        params.put("coupons", order.coupons);
        params.put("sign", order.getRefundSign());
        
        WebServiceClient client = WebServiceClientFactory.getClientHelper();
        String result = client.postString("TsingTuanRefundOrder", SEND_URL, params, order.orderId.toString(), order.teamId.toString());
        Logger.info("返回消息：" + result);
        result = result.trim();
        Matcher m = RESULTCODE_PATTERN.matcher(result);
        if (!m.find()) {
            // 发送失败
            throw new RuntimeException("清团退款不成功:" + result);
        }
    }
}
