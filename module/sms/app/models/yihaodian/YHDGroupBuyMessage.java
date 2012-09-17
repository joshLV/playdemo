package models.yihaodian;

import java.io.Serializable;

/**
 * @author likang
 *         Date: 12-9-15
 */
public class YHDGroupBuyMessage implements Serializable {
    private static final long serialVersionUID = -8571949059752101651L;

    private String orderCode;

    public YHDGroupBuyMessage(String orderCode){
        this.orderCode = orderCode;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    @Override
    public String toString(){
        return "yihaodian group buy job orderCode: " + orderCode;
    }
}
