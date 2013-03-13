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
import models.sales.Sku;
import models.supplier.Supplier;
import util.DateHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;

import static util.DateHelper.afterDays;
import static util.DateHelper.beforeDays;

public class GoodsFactory extends ModelFactory<Goods> {

    @Override
    public Goods define() {
        Goods goods = new Goods();
        Supplier supplier = FactoryBoy.lastOrCreate(Supplier.class);
        goods.name = "Product Name " + FactoryBoy.sequence(Goods.class);
        goods.shortName = "Product Name " + FactoryBoy.sequence(Goods.class);
        goods.title = "Product Title" + FactoryBoy.sequence(Goods.class);
        goods.status = GoodsStatus.ONSALE;
        goods.deleted = DeletedStatus.UN_DELETED;
        goods.isAllShop = false;
        goods.supplierId = supplier.id;
        goods.salePrice = new BigDecimal("8.5");
        goods.beginOnSaleAt= beforeDays(30);
        goods.effectiveAt= beforeDays(30);
        goods.expireAt = afterDays(30);
        goods.endOnSaleAt = afterDays(30);
        goods.faceValue = new BigDecimal("10.00");
        goods.materialType = MaterialType.ELECTRONIC;
        goods.virtualBaseSaleCount = 10l; //虚拟销量
        goods.cumulativeStocks=10l;   //库存
        goods.useWeekDay = "1,2,3,4,5,6,7";
        goods.originalPrice = new BigDecimal("5");
        goods.categories = new HashSet<>();
        goods.shops = new HashSet<>();
        goods.isLottery = Boolean.FALSE;
        goods.unPublishedPlatforms = new HashSet<>();
        goods.categories.add(FactoryBoy.lastOrCreate(Category.class));
        goods.shops.add(FactoryBoy.lastOrCreate(Shop.class));
        goods.setPrompt("prompt");
        goods.setDetails("detail");
        goods.setExhibition("exhib");
        goods.setSupplierDes("des");
        return goods;
    }

    @Factory(name = "noInventory")
    public void defineWithNoInventory(Goods goods) {
        goods = new Goods();
        Supplier supplier = FactoryBoy.lastOrCreate(Supplier.class);
        goods.name = "Product Name " + FactoryBoy.sequence(Goods.class);
        goods.shortName = "Product Name " + FactoryBoy.sequence(Goods.class);
        goods.supplierId = supplier.id;
        goods.salePrice = BigDecimal.TEN;
        goods.expireAt = afterDays(new Date(), 30);
        goods.faceValue = BigDecimal.TEN;
        goods.materialType = MaterialType.REAL;
        goods.cumulativeStocks= -9L;
    }

    @Factory(name = "SupplierId")
    public void defineWithSupplierId(Goods goods) {
    }

    @Factory(name = "Electronic")
    public Goods defineWithElectronic(Goods goods) {
        goods.materialType = MaterialType.ELECTRONIC;
        goods.name = "测试电子商品";
        goods.faceValue = BigDecimal.valueOf(15);
        goods.originalPrice = BigDecimal.valueOf(5);
        goods.salePrice = BigDecimal.valueOf(10);
        goods.supplierId = FactoryBoy.lastOrCreate(Supplier.class).id;
        return goods;
    }


    @Factory(name = "Real")
    public Goods defineWithReal(Goods goods) {
        goods.materialType = MaterialType.REAL;
        goods.name = "测试实物商品";
        goods.faceValue = BigDecimal.valueOf(20);
        goods.originalPrice = BigDecimal.valueOf(8);
        goods.salePrice = BigDecimal.valueOf(15);
        goods.supplierId = FactoryBoy.lastOrCreate(Supplier.class).id;
        goods.sku = FactoryBoy.lastOrCreate(Sku.class);
        goods.skuCount = 2;
        return goods;
    }



    @Factory(name = "goods6")
    public Goods defineWithGoods6(Goods goods) {
        goods.name = "测试商品6";
        goods.originalPrice = BigDecimal.valueOf(100);
        goods.salePrice = BigDecimal.valueOf(80);
        goods.createdAt = DateHelper.t("2012-02-27");
        goods.status = GoodsStatus.ONSALE;
        goods.deleted = DeletedStatus.UN_DELETED;
        goods.supplierId = FactoryBoy.lastOrCreate(Supplier.class).id;
        goods.materialType = MaterialType.REAL;
        goods.effectiveAt = DateHelper.t("2012-02-01");
        goods.expireAt = DateHelper.t("2092-02-02");
        goods.limitNumber = 1;
        goods.imagePath = "/0/0/133/origin.jpg";
        return goods;

    }




}
