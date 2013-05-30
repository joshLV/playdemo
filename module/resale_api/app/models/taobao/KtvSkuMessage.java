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
    public Long ktvProductGoodsId;

    public KtvSkuMessage(Long scheduledId, Long ktvProductGoodsId) {
        this.scheduledId = scheduledId;
        this.ktvProductGoodsId = ktvProductGoodsId;
    }


    @Override
    public String toString() {
        return "taobao sku message:scheduledId= " + scheduledId + ",ktvProductGoodsId=" + ktvProductGoodsId;
    }
}
