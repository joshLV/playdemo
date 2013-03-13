package factory.sales;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.sales.InventoryStock;
import models.sales.InventoryStockItem;
import models.sales.Sku;

import java.util.Date;

import static util.DateHelper.afterDays;
import static util.DateHelper.beforeDays;

/**
 * 库存变动明细.
 * <p/>
 * User: wangjia
 * Date: 13-3-12
 * Time: 下午3:40
 */
public class InventoryStockItemFactory extends ModelFactory<InventoryStockItem> {
    @Override
    public InventoryStockItem define() {
        InventoryStockItem stockItem = new InventoryStockItem(FactoryBoy.lastOrCreate(InventoryStock.class));
        stockItem.changeCount = 10l;
        stockItem.remainCount = stockItem.changeCount;
        stockItem.effectiveAt = beforeDays(30);
        stockItem.expireAt = afterDays(30);
        stockItem.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED;
        stockItem.createdAt = new Date();
        stockItem.sku = FactoryBoy.lastOrCreate(Sku.class);
        return stockItem;
    }


}
