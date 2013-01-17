package factory.sales;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.sales.Category;
import models.sales.Goods;
import models.sales.GoodsHistory;
import models.sales.GoodsStatus;
import models.sales.MaterialType;
import models.sales.Shop;
import models.supplier.Supplier;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;

import static util.DateHelper.afterDays;

/**
 * User: wangjia
 * Date: 12-10-12
 * Time: 下午3:42
 */
public class GoodsHistoryFactory extends ModelFactory<GoodsHistory> {
    @Override
    public GoodsHistory define() {
        GoodsHistory goodsHistory = new GoodsHistory();
        Supplier supplier = FactoryBoy.lastOrCreate(Supplier.class);
        Goods goods = FactoryBoy.lastOrCreate(Goods.class);
        goodsHistory.goodsId = goods.id;
        goodsHistory.name = "Product Name " + FactoryBoy.sequence(Goods.class);
        goodsHistory.title = "Product Title" + FactoryBoy.sequence(Goods.class);
        goodsHistory.status = GoodsStatus.ONSALE;
        goodsHistory.isAllShop = false;
        goodsHistory.supplierId = supplier.id;
        goodsHistory.salePrice = BigDecimal.TEN;
        goodsHistory.expireAt = afterDays(new Date(), 30);
        goodsHistory.faceValue = BigDecimal.TEN;
        goodsHistory.materialType = MaterialType.REAL;
        goodsHistory.baseSale = 100L;
        goodsHistory.useWeekDay = "1,2,3,4,5,6,7";
        goodsHistory.originalPrice = new BigDecimal("5");
        goodsHistory.categories = new HashSet<>();
        goodsHistory.shops = new HashSet<>();
        goodsHistory.categories.add(FactoryBoy.lastOrCreate(Category.class));
        goodsHistory.shops.add(FactoryBoy.lastOrCreate(Shop.class));
        return goodsHistory;
    }
}
