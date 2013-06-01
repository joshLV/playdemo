package models.taobao;

import java.io.Serializable;

/**
 * @author yanjy
 */
public class KtvSkuMessage implements Serializable {
    private static final long serialVersionUID = -8973123251882104951L;
    public Long productGoodsId;

    public KtvSkuMessage(Long productGoodsId) {
        this.productGoodsId = productGoodsId;
    }


    @Override
    public String toString() {
        return "taobao sku message:productGoodsId= " + productGoodsId;
    }
}
