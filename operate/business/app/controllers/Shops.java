package controllers;

import models.sales.Shop;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 4/12/12
 * Time: 1:57 PM
 */
@With(OperateRbac.class)
@ActiveNavigation("goods_index")
public class Shops extends Controller {

    @ActiveNavigation("goods_add")
    public static void showGoodsShops(Long supplierId) {
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        render(shopList);
    }
}
