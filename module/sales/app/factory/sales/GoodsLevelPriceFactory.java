package factory.sales;

import factory.ModelFactory;
import models.resale.ResalerLevel;
import models.sales.GoodsLevelPrice;

import java.math.BigDecimal;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-19
 * Time: 下午2:26
 */
public class GoodsLevelPriceFactory extends ModelFactory<GoodsLevelPrice> {
    @Override
    public GoodsLevelPrice define() {
        GoodsLevelPrice price = new GoodsLevelPrice();
        price.level = ResalerLevel.NORMAL;
        price.price = BigDecimal.ONE;
        return price;
    }

}
