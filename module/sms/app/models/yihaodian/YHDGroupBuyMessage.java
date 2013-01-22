package models.yihaodian;

import java.io.Serializable;

/**
 * @author likang
 *         Date: 12-9-15
 */
public class YHDGroupBuyMessage implements Serializable {
    private static final long serialVersionUID = -8571949059752101651L;

    private Long orderId;

    public YHDGroupBuyMessage(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "yihaodian group buy job orderId: " + orderId;
    }
}
