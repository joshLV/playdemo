package models.taobao;

import java.io.Serializable;

/**
 * @author likang
 *         Date: 12-11-29
 */
public class TaobaoCouponMessage implements Serializable {
    private static final long serialVersionUID = -8173923259882104951L;
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
}
