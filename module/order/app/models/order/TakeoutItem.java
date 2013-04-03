package models.order;

import models.sales.Goods;
import models.sales.Sku;

/**
 * 出库表
 * <p/>
 * User: sujie
 * Date: 3/12/13
 * Time: 2:11 PM
 */
public class TakeoutItem {
    public Sku sku;
    public Long count;

    public TakeoutItem(Goods goods, Long count) {
        System.out.println("goods.id:" + goods.id);
        this.sku = goods.sku;
        this.count = count * goods.skuCount;
    }

    public TakeoutItem(Sku sku, Long count) {
        this.sku = sku;
        this.count = count;
    }

}
