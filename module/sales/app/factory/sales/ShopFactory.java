package factory.sales;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.sales.Shop;
import models.supplier.Supplier;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-20
 * Time: 下午5:26
 * To change this template use File | Settings | File Templates.
 */
public class ShopFactory extends ModelFactory<Shop> {

    @Override
    public Shop define() {

        Shop shop = new Shop();
        Supplier supplier = FactoryBoy.create(Supplier.class);
        shop.supplierId = supplier.id;
        shop.areaId = "021";
        shop.name = "shop0";
        shop.deleted = DeletedStatus.UN_DELETED;

        return shop;
    }

    @Factory(name = "SupplierId")
    public Shop defineWithSupplierId(Shop shop){

        shop.areaId = "021";
        shop.name = "shop0";
        shop.deleted = DeletedStatus.UN_DELETED;

        return shop;

    }
}
