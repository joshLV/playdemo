package controllers.resale;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import controllers.OperateRbac;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import models.wuba.WubaUtil;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Collection;

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

        String allCategoriesJson = WubaUtil.allProductTypesJsonCache();

        Collection<Shop> shopList = goods.getShopList();
        Supplier supplier = Supplier.findById(goods.supplierId);

        render(goods, allCategoriesJson, shopList, supplier);
    }

    @ActiveNavigation("resale_partner_product")
    public static void upload(Long outerId ) {

    }

    @ActiveNavigation("resale_partner_product")
    public static void showProducts(Long goodsId) {

    }

}

