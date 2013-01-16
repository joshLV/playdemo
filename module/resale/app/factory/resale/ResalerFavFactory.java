package factory.resale;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.resale.Resaler;
import models.resale.ResalerFav;
import models.sales.Goods;

/**
 * TODO.
 * <p/>
 * User: yanjy
 * Date: 12-11-15
 * Time: 下午5:23
 */
public class ResalerFavFactory extends ModelFactory<ResalerFav> {
    @Override
    public ResalerFav define() {
        Resaler resaler = FactoryBoy.lastOrCreate(Resaler.class);
        Goods goods = FactoryBoy.lastOrCreate(Goods.class);
        ResalerFav resalerFav = new ResalerFav(resaler, goods);

        return resalerFav;
    }
}
