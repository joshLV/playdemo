package factory.sales;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.order.OuterOrderPartner;
import models.sales.Goods;
import models.sales.GoodsThirdSupport;

import java.util.Date;

/**
 * TODO.
 * <p/>
 * User: yanjy
 * Date: 12-11-22
 * Time: 下午4:17
 */
public class GoodsThirdSupportFactory extends ModelFactory<GoodsThirdSupport> {
    @Override
    public GoodsThirdSupport define() {
        Goods goods = FactoryBoy.create(Goods.class);
        GoodsThirdSupport support = new GoodsThirdSupport();
        support.partner = OuterOrderPartner.DD;
        support.goods = goods;
        support.goodsData = "";
        support.createdAt = new Date();
        support.save();
        return support;
    }
}
