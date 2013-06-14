package models.taobao;

import models.mq.QueueIDMessage;
import play.Play;
import util.mq.MQPublisher;

import java.io.Serializable;

/**
 * @author likang
 *         Date: 12-11-29
 */
public class TaobaoCouponMessage extends QueueIDMessage implements Serializable {
    private static final long serialVersionUID = -8173232582104951L;
    public static final String MQ_KEY = Play.mode.isProd() ? "order.taobao.coupon" : "order.taobao.coupon";

    public Long outerOrderId;

    public TaobaoCouponMessage(Long outerOrderId) {
        this.outerOrderId = outerOrderId;
    }

    public Long getOuterOrderId() {
        return outerOrderId;
    }

    public void setOuterOrderId(Long outerOrderId) {
        this.outerOrderId = outerOrderId;
    }

    @Override
    public String toString() {
        return "taobao coupon message: " + outerOrderId;
    }

    @Override
    public String messageId() {
        return MQ_KEY + this.getOuterOrderId();
    }

    public void send(){
        MQPublisher.publish(MQ_KEY, this);
    }
}
