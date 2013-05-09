package factory.ktv;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.ktv.KtvTaobaoSku;
import models.sales.Goods;

import java.math.BigDecimal;

/**
 * User: yan
 * Date: 13-5-9
 * Time: 下午3:10
 */
public class KtvTaobaoSkuFactory extends ModelFactory<KtvTaobaoSku> {
    @Override
    public KtvTaobaoSku define() {
        KtvTaobaoSku sku = new KtvTaobaoSku();
        sku.setDate("5月10");
        sku.setRoomType("27426219:3442354");
        sku.setTimeRange("8点至10点");
        sku.goods = FactoryBoy.lastOrCreate(Goods.class);
        sku.price = BigDecimal.TEN;
        sku.quantity = 10;
        return sku;
    }

    @Factory(name = "update")
    public KtvTaobaoSku update(KtvTaobaoSku sku) {
        sku.price=BigDecimal.ONE;
        return sku;
    }
    @Factory(name = "date")
    public KtvTaobaoSku otherDate(KtvTaobaoSku sku) {
        sku.setDate("5月11");
        sku.setTimeRange("12点至15点");
        sku.setRoomType("27426219:6312905");
        return sku;
    }
}
