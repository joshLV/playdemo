package factory.sales;

import models.sales.Area;
import models.sales.Shop;
import models.supplier.Supplier;
import util.DateHelper;

import com.uhuila.common.constants.DeletedStatus;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;

/**
 * User: hejun
 * Date: 12-8-20
 * Time: 下午5:26
 */
public class ShopFactory extends ModelFactory<Shop> {

    @Override
    public Shop define() {
        Shop shop = new Shop();
        Supplier supplier = FactoryBoy.lastOrCreate(Supplier.class);
        shop.deleted = DeletedStatus.UN_DELETED;
        shop.supplierId = supplier.id;
        shop.address = "宛平南路2号";
        shop.phone = "02100000";
        shop.latitude = "121.12888";
        shop.longitude = "31.12888";
        shop.createdAt = DateHelper.beforeDays(20);
        shop.updatedAt = DateHelper.beforeDays(15);
        Area area = FactoryBoy.lastOrCreate(Area.class);
		shop.areaId = area.id;
        shop.name = "测试店";
        shop.deleted = DeletedStatus.UN_DELETED;
        shop.managerMobiles="15026682165";
        shop.lockVersion = 0;
        return shop;
    }

    @Factory(name = "SupplierId")
    public Shop defineWithSupplierId(Shop shop) {
        shop.areaId = "021";
        shop.name = "shop0";
        shop.deleted = DeletedStatus.UN_DELETED;

        return shop;
    }
}
