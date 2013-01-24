package models.thirdtuan;

import models.RabbitMQConsumerWithTx;
import models.tsingtuan.TsingTuanOrder;
import models.tsingtuan.TsingTuanSendOrder;
import play.Logger;
import util.ws.WebServiceRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// @OnApplicationStart(async = true)
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

    @Override
    protected int retries() {
        return 10;
    }

    @Override
    protected String routingKey() {
        return this.queue();
    }

    public static final String SEND_URL = "http://www.tsingtuan.com/outer/shihui/order.php";

    private final Pattern RESULTCODE_PATTERN = Pattern.compile("^0\\|");

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

        String result = WebServiceRequest.url(SEND_URL)
                .type("TsingTuanSendOrder").params(params)
                .addKeyword(order.orderId).addKeyword(order.teamId)
                .postString();

        Logger.info("返回消息：" + result);
        result = result.trim();
        Matcher m = RESULTCODE_PATTERN.matcher(result);
        if (!m.find()) {
            // 发送失败
            throw new RuntimeException("清团订单同步不成功:" + result);
        }
    }
}
