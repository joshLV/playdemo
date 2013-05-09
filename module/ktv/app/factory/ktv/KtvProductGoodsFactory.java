package factory.ktv;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.ktv.KtvProduct;
import models.ktv.KtvProductGoods;
import models.sales.Goods;
import models.sales.Shop;

/**
 * User: yan
 * Date: 13-5-9
 * Time: 下午3:11
 */
public class KtvProductGoodsFactory extends ModelFactory<KtvProductGoods> {
    @Override
    public KtvProductGoods define() {
        KtvProductGoods productGoods = new KtvProductGoods();
        productGoods.shop = FactoryBoy.lastOrCreate(Shop.class);
        productGoods.goods = FactoryBoy.lastOrCreate(Goods.class);
        productGoods.product = FactoryBoy.lastOrCreate(KtvProduct.class);
        return productGoods;
    }
}
