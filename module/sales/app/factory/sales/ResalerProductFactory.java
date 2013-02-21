package factory.sales;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.order.OuterOrderPartner;
import models.sales.ResalerProduct;
import models.sales.Goods;

/**
 * @author likang
 *         Date: 13-1-11
 */
public class ResalerProductFactory extends ModelFactory<ResalerProduct> {
    @Override
    public ResalerProduct define() {
        ResalerProduct resalerProduct = new ResalerProduct();
        resalerProduct.partner = OuterOrderPartner.TB;
        resalerProduct.goods = FactoryBoy.lastOrCreate(Goods.class);
        resalerProduct.save();
        resalerProduct.goodsLinkId =  resalerProduct.id + 10000;
        return resalerProduct.save();
    }
}
