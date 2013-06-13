package factory.sales;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.sales.Goods;
import models.sales.GoodsProperty;
import models.supplier.Supplier;
import models.supplier.SupplierProperty;

/**
 * User: yan
 * Date: 13-6-8
 * Time: 下午5:34
 */
public class GoodsPropertyFactory extends ModelFactory<GoodsProperty> {

    @Override
    public GoodsProperty define() {
        GoodsProperty property = new GoodsProperty(FactoryBoy.lastOrCreate(Goods.class), Goods.SECONDARY_VERIFICATION, "1");
        return property;
    }
}
