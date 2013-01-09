package controllers.resale;

import controllers.OperateRbac;
import models.order.OuterOrderPartner;
import models.resale.ResalePartnerProduct;
import models.sales.Goods;
import operate.rbac.annotations.ActiveNavigation;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * @author likang
 *         Date: 13-1-8
 */
@With(OperateRbac.class)
@ActiveNavigation("resale_partner_product")
public class YHDGroupBuyProduct extends Controller{
    public static void showUpload(Long goodsId) {
        Goods goods = Goods.findById(goodsId);
        render(goods);
    }

    @ActiveNavigation("resale_partner_product")
    public static void showProducts(Long goodsId) {
        Goods goods = Goods.findById(goodsId);
        if (goods == null) {
            Logger.info("goods not found");
            error("商品不存在");
        }
        List<ResalePartnerProduct> products = ResalePartnerProduct.find("byGoodsAndPartner",goods, OuterOrderPartner.YHD).fetch();
        render(products);
    }
}
