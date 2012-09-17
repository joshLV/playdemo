package models.yihaodian;

import java.io.Serializable;

/**
 * @author likang
 */
public class YihaodianJobMessage implements Serializable {
    private static final long serialVersionUID = -8579949259782101651L;

    private Long orderId;   //一号店订单的orderId属性 不是存在我们数据库中的ID

    public YihaodianJobMessage(Long orderId){
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString(){
        return "yihaodian job orderId: " + orderId;
    }
}
