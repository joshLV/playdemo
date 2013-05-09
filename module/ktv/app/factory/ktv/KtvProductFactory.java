package factory.ktv;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvProduct;
import models.supplier.Supplier;

/**
 * User: yan
 * Date: 13-5-9
 * Time: 下午3:12
 */
public class KtvProductFactory extends ModelFactory<KtvProduct> {
    @Override
    public KtvProduct define() {
        KtvProduct product = new KtvProduct();
        product.duration = 3;
        product.supplier = FactoryBoy.lastOrCreate(Supplier.class);
        product.name = "欢唱3小时候";
        return product;
    }

    @Factory(name = "twoHours")
    public KtvProduct twoHours(KtvProduct product) {
        product.duration = 2;
        product.name = "欢唱2小时候";
        return product;
    }
}
