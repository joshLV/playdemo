package controllers.resale;

import controllers.OperateRbac;
import models.sales.Goods;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * @author likang
 *         Date: 13-1-16
 */
@With(OperateRbac.class)
@ActiveNavigation("resale_partner_product")
public class WubaGroupBuyProducts extends Controller {
    @ActiveNavigation("resale_partner_product")
    public static void showUpload(Long goodsId) {
        Goods goods = Goods.findById(goodsId);

        render(goods);
    }

    @ActiveNavigation("resale_partner_product")
    public static void upload(Long outerId ) {

    }

    @ActiveNavigation("resale_partner_product")
    public static void showProducts(Long goodsId) {

    }

}

