package models.taobao;

import play.Play;
import util.mq.MQPublisher;

/**
 * @author likang
 *         Date: 12-11-29
 */
public class TaobaoCouponMessageUtil {
    public static final String QUEUE_NAME = Play.mode.isProd() ? "taobao_coupon" : "taobao_coupon_dev";
    private TaobaoCouponMessageUtil(){}

    public static void send(Long outerOrderId){
        MQPublisher.publish(QUEUE_NAME, new TaobaoCouponMessage(outerOrderId));
    }
}
