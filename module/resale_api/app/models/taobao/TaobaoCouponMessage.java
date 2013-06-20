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
    public static final String MQ_KEY = Play.mode.isProd() ? "order.v2.taobao.coupon" : "order.v2.taobao.coupon_dev";

    public Long outerOrderId;

    /**
     * 保存outerOrderId的lockVersion，在consumer中检查，如果不一致则让consumer重试.
     */
    public Integer lockVersion;

    public TaobaoCouponMessage(Long outerOrderId) {
        this.outerOrderId = outerOrderId;
    }

    public Long getOuterOrderId() {
        return outerOrderId;
    }

    public void setOuterOrderId(Long outerOrderId) {
        this.outerOrderId = outerOrderId;
    }

    public TaobaoCouponMessage lockVersion(Integer value) {
        this.lockVersion = value;
        return this;
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
