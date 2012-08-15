package factory.sales;

import java.math.BigDecimal;
import java.util.UUID;
import models.sales.Goods;
import factory.ModelFactory;

public class GoodsFactory extends ModelFactory<Goods> {

    @Override
    public Goods define() {
        Goods goods = new Goods();
        goods.name = "Product Name " + UUID.randomUUID().toString();
        goods.salePrice = BigDecimal.TEN;
        return goods;
    }

    
}
