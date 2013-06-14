package models.thirdtuan;

import models.mq.RabbitMQConsumerWithTx;
import models.tsingtuan.TsingTuanOrder;
import models.tsingtuan.TsingTuanSendOrder;
import play.Logger;
import util.ws.WebServiceRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@OnApplicationStart(async = true)
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

        String result = WebServiceRequest.url(SEND_URL)
                .type("TsingTuanRefundOrder").params(params)
                .addKeyword(order.orderId).postString();

        Logger.info("返回消息：" + result);
        result = result.trim();
        Matcher m = RESULTCODE_PATTERN.matcher(result);
        if (!m.find()) {
            // 发送失败
            throw new RuntimeException("清团退款不成功:" + result);
        }
    }
}
