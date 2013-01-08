package factory.resale;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.resale.Resaler;
import models.resale.ResalerCart;
import models.sales.Goods;

/**
 * @author likang
 *         Date: 13-1-7
 */
public class ResalerCartFactory extends ModelFactory<ResalerCart>{
    @Override
    public ResalerCart define() {
        Resaler resaler = FactoryBoy.lastOrCreate(Resaler.class);
        Goods goods = FactoryBoy.lastOrCreate(Goods.class);

        return new ResalerCart(resaler, goods, "13472581853", 2L);
    }
}
