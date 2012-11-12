package factory.sales;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.sales.BrowsedGoods;
import models.sales.Goods;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 11/9/12
 * Time: 4:21 PM
 */
public class BrowsedGoodsFactory  extends ModelFactory<BrowsedGoods> {
    @Override
    public BrowsedGoods define() {
        BrowsedGoods browsedGoods = new BrowsedGoods(null,"aaaaaaaaaaaaaaaaa",FactoryBoy.lastOrCreate(Goods.class));
        return browsedGoods;
    }

}