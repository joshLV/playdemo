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
        this.sku = goods.sku;
        this.count = count;
    }

}
