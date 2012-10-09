package factory.sales;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.sales.*;
import models.supplier.Supplier;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static util.DateHelper.afterDays;

public class GoodsFactory extends ModelFactory<Goods> {

    @Override
    public Goods define() {
        Goods goods = new Goods();
        Supplier supplier = FactoryBoy.lastOrCreate(Supplier.class);
        goods.name = "Product Name " + FactoryBoy.sequence(Goods.class);
        goods.title = "Product Title" + FactoryBoy.sequence(Goods.class);
        goods.status = GoodsStatus.ONSALE;
        goods.deleted = DeletedStatus.UN_DELETED;
        goods.isAllShop = false;
        goods.supplierId = supplier.id;
        goods.salePrice = BigDecimal.TEN;
        goods.expireAt = afterDays(new Date(), 30);
        goods.faceValue = BigDecimal.TEN;
        goods.materialType = MaterialType.REAL;
        goods.baseSale = 100L;
        goods.saleCount = 10;
        goods.useWeekDay = "1,2,3,4,5,6,7";
        goods.originalPrice = new BigDecimal("5");
        goods.categories = new HashSet<>();
        goods.shops = new HashSet<>();
        goods.categories.add(FactoryBoy.last(Category.class));
        goods.shops.add(FactoryBoy.last(Shop.class));
        return goods;
    }
    
    @Factory(name = "noInventory")
    public void defineWithNoInventory(Goods goods) {
        goods.baseSale = -9L;
    }

    @Factory(name = "SupplierId")
    public void defineWithSupplierId(Goods goods) {
    }

    @Factory(name = "Electronic")
    public void defineWithElectronic(Goods goods) {
        goods.materialType = MaterialType.ELECTRONIC;
    }
}
