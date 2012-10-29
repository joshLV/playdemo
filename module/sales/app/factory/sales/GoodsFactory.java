package factory.sales;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.sales.Category;
import models.sales.Goods;
import models.sales.GoodsStatus;
import models.sales.MaterialType;
import models.sales.Shop;
import models.supplier.Supplier;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;

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
        goods.salePrice = new BigDecimal("8.5");
        goods.expireAt = afterDays(30);
        goods.faceValue = new BigDecimal("10.00");
        goods.materialType = MaterialType.ELECTRONIC;
        goods.baseSale = 100L;
        goods.saleCount = 10;
        goods.useWeekDay = "1,2,3,4,5,6,7";
        goods.originalPrice = new BigDecimal("5");
        goods.categories = new HashSet<>();
        goods.shops = new HashSet<>();
        goods.isLottery = Boolean.FALSE;
        goods.unPublishedPlatforms = new HashSet<>();
        goods.categories.add(FactoryBoy.lastOrCreate(Category.class));
        goods.shops.add(FactoryBoy.lastOrCreate(Shop.class));
        return goods;
    }
    
    @Factory(name = "noInventory")
    public void defineWithNoInventory(Goods goods) {
        goods = new Goods();
        Supplier supplier = FactoryBoy.lastOrCreate(Supplier.class);
        goods.name = "Product Name " + FactoryBoy.sequence(Goods.class);
        goods.supplierId = supplier.id;
        goods.salePrice = BigDecimal.TEN;
        goods.expireAt = afterDays(new Date(), 30);
        goods.faceValue = BigDecimal.TEN;
        goods.materialType = MaterialType.REAL;
        goods.baseSale = -9L;
    }

    @Factory(name = "SupplierId")
    public void defineWithSupplierId(Goods goods) {
    }

    @Factory(name = "Electronic")
    public void defineWithElectronic(Goods goods) {
        goods.materialType = MaterialType.ELECTRONIC;
    }
    

    @Factory(name = "Real")
    public void defineWithReal(Goods goods) {
        goods.materialType = MaterialType.REAL;
    }
    
}
