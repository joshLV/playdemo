package factory.resale;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.admin.OperateUser;
import models.order.OuterOrderPartner;
import models.resale.ResalerProduct;
import models.sales.Goods;

/**
 * @author likang
 *         Date: 13-1-11
 */
public class ResalerProductFactory extends ModelFactory<ResalerProduct>{
    @Override
    public ResalerProduct define() {
        ResalerProduct resalerProduct = new ResalerProduct();
        resalerProduct.partner = OuterOrderPartner.TB;
        resalerProduct.creatorId = FactoryBoy.lastOrCreate(OperateUser.class).id;
        resalerProduct.lastModifierId = resalerProduct.creatorId;
        resalerProduct.goods = FactoryBoy.lastOrCreate(Goods.class);
        resalerProduct.goodsLinkId = resalerProduct.goods.id;

        return resalerProduct;
    }
}
