package models.yihaodian;

import java.io.Serializable;

/**
 * @author likang
 */
public class YihaodianJobMessage implements Serializable {
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
