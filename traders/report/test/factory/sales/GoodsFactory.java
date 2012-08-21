package factory.sales;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.sales.Goods;

import java.math.BigDecimal;

public class GoodsFactory extends ModelFactory<Goods> {

    @Override
    public Goods define() {
        Goods goods = new Goods();
        goods.name = "Product Name " + FactoryBoy.sequence(Goods.class);
        goods.salePrice = BigDecimal.TEN;
        return goods;
    }


}
