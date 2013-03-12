package factory.sales;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.sales.InventoryStock;
import models.sales.Sku;
import models.supplier.Supplier;

/**
 * 库存变动单
 * <p/>
 * User: wangjia
 * Date: 13-3-12
 * Time: 上午10:58
 */
public class InventoryStockFactory extends ModelFactory<InventoryStock> {
    @Override
    public InventoryStock define() {
        InventoryStock stock = new InventoryStock();
        stock.serialNo = "J2013022601";
        stock.dateOfSerialNo = "20130226";
        stock.sequenceCode = "01";
        stock.supplier = FactoryBoy.lastOrCreate(Supplier.class);
        stock.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED;
        stock.createdBy = "test-person";
        return stock;
    }
}
