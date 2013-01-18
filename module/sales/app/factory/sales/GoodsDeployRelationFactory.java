package factory.sales;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.order.OuterOrderPartner;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;

import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-11-15
 * Time: 下午3:37
 */
public class GoodsDeployRelationFactory extends ModelFactory<GoodsDeployRelation> {
    @Override
    public GoodsDeployRelation define() {
        Goods goods = FactoryBoy.lastOrCreate(Goods.class);
        GoodsDeployRelation deployRelation = new GoodsDeployRelation();
        deployRelation.partner = OuterOrderPartner.DD;
        deployRelation.goods = goods;
        deployRelation.createAt = new Date();
        deployRelation.save();
        deployRelation.linkId = deployRelation.id + 10000;
        return deployRelation;
    }
}
