package factory.sales;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.sales.Goods;
import models.sales.GoodsImages;

/**
 * <p/>
 * User: yanjy
 * Date: 12-11-8
 * Time: 下午3:43
 */
public class GoodsImagesFactory extends ModelFactory<GoodsImages> {
    @Override
    public GoodsImages define() {
        Goods goods = FactoryBoy.create(Goods.class);
        GoodsImages images = new GoodsImages(goods, "/opt/1.jpg");
        return images;
    }
}
