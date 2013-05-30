package models.taobao;

import models.ktv.KtvProduct;
import models.ktv.KtvProductGoods;

import java.io.Serializable;

/**
 * @author yanjy
 */
public class KtvSkuMessage implements Serializable {
    private static final long serialVersionUID = -8973123251882104951L;
    public Long scheduledId;

    public KtvSkuMessage(Long scheduledId) {
        this.scheduledId = scheduledId;
    }


    @Override
    public String toString() {
        return "taobao sku message:scheduledId= " + scheduledId ;
    }
}
