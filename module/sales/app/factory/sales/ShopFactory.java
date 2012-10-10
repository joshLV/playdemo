package factory.sales;

import static util.DateHelper.t;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import models.sales.Shop;
import models.supplier.Supplier;

import com.uhuila.common.constants.DeletedStatus;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;

public class ShopFactory extends ModelFactory<Shop> {

	@Override
	public Shop define() {
		Shop shop = new Shop();
		Supplier supplier = FactoryBoy.lastOrCreate(Supplier.class);
		shop.deleted = DeletedStatus.UN_DELETED;
		shop.supplierId = supplier.id;
		shop.address = "宛平南路2号";
		shop.phone = "02100000";
		shop.latitude = 0.0f;
		shop.longitude = 0.0f;
		shop.createdAt = t("2012-02-29 16:33:18");
		shop.updatedAt = t("2012-02-29 16:44:33");

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
