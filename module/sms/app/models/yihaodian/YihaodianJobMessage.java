package models.yihaodian;

import java.io.Serializable;

/**
 * @author likang
 */
public class YihaodianJobMessage implements Serializable {
    private static final long serialVersionUID = -8579949259782101651L;

    private Long yihaodianOrderId;

    public YihaodianJobMessage(Long yihaodianOrderId){
        this.yihaodianOrderId = yihaodianOrderId;
    }

    public Long getYihaodianOrderId() {
        return yihaodianOrderId;
    }

    public void setYihaodianOrderId(Long yihaodianOrderId) {
        this.yihaodianOrderId = yihaodianOrderId;
    }

    @Override
    public String toString(){
        return "yihaodian job orderId: " + yihaodianOrderId;
    }
}
