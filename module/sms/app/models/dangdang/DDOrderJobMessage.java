package models.dangdang;

import java.io.Serializable;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-17
 * Time: 下午3:19
 */
public class DDOrderJobMessage implements Serializable {
    private static final long serialVersionUID = -8579235259782608621L;

    private Long orderId;

    public DDOrderJobMessage(Long orderId) {
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
        return "dangdang job orderCode: " + orderId;
    }
}
