package controllers.resale;

import controllers.OperateRbac;
import models.admin.OperateUser;
import models.order.OuterOrderPartner;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import models.sales.Shop;
import models.supplier.Supplier;
import models.wuba.WubaUtil;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
    public static void upload(Long groupbuyId, String[] cityIds ) {
        OperateUser operateUser = OperateRbac.currentUser();
        Goods goods = Goods.findById(groupbuyId);
        if (goods == null) {
            notFound();
        }

        GoodsDeployRelation relation = GoodsDeployRelation.generate(goods, OuterOrderPartner.WB);

        Map<String, String> postParams = request.params.allSimple();
        postParams.remove("body");
        postParams.put("groupbuyId", String.valueOf(relation.linkId));

        Map<String, Object> requestParams = new HashMap<>();

        for(Map.Entry<String, String> entry : postParams.entrySet()) {

        }

        WubaUtil.sendRequest(requestParams,"emc.groupbuy.addgroupbuy");
    }

    @ActiveNavigation("resale_partner_product")
    public static void showProducts(Long goodsId) {

    }
}

