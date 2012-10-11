package factory.sales;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import models.sales.Shop;
import models.supplier.Supplier;

import com.uhuila.common.constants.DeletedStatus;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-20
 * Time: 下午5:26
 */
public class ShopFactory extends ModelFactory<Shop> {

    @Override
    public Shop define() {

        Shop shop = new Shop();
        Supplier supplier = FactoryBoy.lastOrCreate(Supplier.class);
        shop.deleted=DeletedStatus.UN_DELETED;
        shop.supplierId = supplier.id;
        shop.address = "宛平南路2号";
        shop.phone = "02100000";
        shop.latitude = "121.12888";
        shop.longitude = "31.12888";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            shop.createdAt = dateFormat.parse("2012-02-29 16:33:18");
        } catch (ParseException e) {
            //ignore
        }

        try {
            shop.updatedAt = dateFormat.parse("2012-02-29 16:44:33");
        } catch (ParseException e) {
            //ignore
        }

        shop.areaId = "021";
        shop.name = "shop0";
        shop.deleted = DeletedStatus.UN_DELETED;
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
