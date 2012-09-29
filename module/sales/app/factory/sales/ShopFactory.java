package factory.sales;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.sales.Shop;
import models.supplier.Supplier;

import java.text.ParseException;
import java.text.SimpleDateFormat;

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
        Supplier supplier = FactoryBoy.last(Supplier.class);
        shop.deleted=DeletedStatus.UN_DELETED;
        shop.supplierId = supplier.id;
        shop.address="宛平南路2号";
        shop.phone="02100000";
        shop.latitude=0.0f;
        shop.longitude=0.0f;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            shop.createdAt =  dateFormat.parse("2012-02-29 16:33:18");
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            shop.updatedAt =  dateFormat.parse("2012-02-29 16:44:33");
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        shop.areaId = "021";
        shop.name = "shop0";
        shop.deleted = DeletedStatus.UN_DELETED;
        shop.lockVersion=0;
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
