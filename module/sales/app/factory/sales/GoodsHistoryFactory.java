package factory.sales;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import models.sales.*;
import models.supplier.Supplier;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;

import static util.DateHelper.afterDays;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-10-12
 * Time: 下午3:42
 * To change this template use File | Settings | File Templates.
 */
public class GoodsHistoryFactory extends ModelFactory<GoodsHistory> {
    @Override
    public GoodsHistory define() {
        GoodsHistory goods = new GoodsHistory();
        Supplier supplier = FactoryBoy.lastOrCreate(Supplier.class);
        goods.name = "Product Name " + FactoryBoy.sequence(Goods.class);
        goods.title = "Product Title" + FactoryBoy.sequence(Goods.class);
        goods.status = GoodsStatus.ONSALE;
        goods.isAllShop = false;
        goods.supplierId = supplier.id;
        goods.salePrice = BigDecimal.TEN;
        goods.expireAt = afterDays(new Date(), 30);
        goods.faceValue = BigDecimal.TEN;
        goods.materialType = MaterialType.REAL;
        goods.baseSale = 100L;
        goods.useWeekDay = "1,2,3,4,5,6,7";
        goods.originalPrice = new BigDecimal("5");
        goods.categories = new HashSet<>();
        goods.shops = new HashSet<>();
        goods.categories.add(FactoryBoy.lastOrCreate(Category.class));
        goods.shops.add(FactoryBoy.lastOrCreate(Shop.class));
        return goods;
    }
}
