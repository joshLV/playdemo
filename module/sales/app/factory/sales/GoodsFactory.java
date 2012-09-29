package factory.sales;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.sales.Category;
import models.sales.Goods;
import models.sales.MaterialType;
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
        Supplier supplier = FactoryBoy.create(Supplier.class);
        goods.name = "Product Name " + FactoryBoy.sequence(Goods.class);
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
        goods.categories.add(FactoryBoy.last(Category.class));
        return goods;
    }

    @Factory(name = "noInventory")
    public Goods defineWithNoInventory(Goods goods) {
        goods = new Goods();
        Supplier supplier = FactoryBoy.create(Supplier.class);
        goods.name = "Product Name " + FactoryBoy.sequence(Goods.class);
        goods.supplierId = supplier.id;
        goods.salePrice = BigDecimal.TEN;
        goods.expireAt = afterDays(new Date(), 30);
        goods.faceValue = BigDecimal.TEN;
        goods.materialType = MaterialType.REAL;
        goods.baseSale = -9L;
        goods.saleCount = 10;
        return goods;

    }

    @Factory(name = "SupplierId")
    public Goods defineWithSupplierId(Goods goods) {
        goods.name = "Product Name " + FactoryBoy.sequence(Goods.class);
        goods.salePrice = BigDecimal.TEN;
        goods.expireAt = afterDays(new Date(), 30);
        goods.materialType = MaterialType.REAL;

        return goods;

    }

    @Factory(name = "Electronic")
    public Goods defineWithElectronic(Goods goods) {
        goods.name = "Product Name " + FactoryBoy.sequence(Goods.class);
        goods.salePrice = BigDecimal.TEN;
        goods.expireAt = afterDays(new Date(), 30);
        goods.materialType = MaterialType.ELECTRONIC;

        return goods;

    }
}
