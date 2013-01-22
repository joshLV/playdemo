package controllers.resale;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import controllers.OperateRbac;
import models.admin.OperateUser;
import models.order.OuterOrderPartner;
import models.resale.ResalerProduct;
import models.resale.ResalerProductJournal;
import models.resale.ResalerProductJournalType;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import models.sales.Shop;
import models.supplier.Supplier;
import models.wuba.WubaUtil;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.*;

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
    public static void upload(long groupbuyId, int shopSize) {
        OperateUser operateUser = OperateRbac.currentUser();
        Goods goods = Goods.findById(groupbuyId);
        if (goods == null) {
            notFound();
        }

        //先将所有的参数认为是是团购信息参数
        Map<String, String> groupbuyInfoParams = params.allSimple();
        groupbuyInfoParams.remove("body");
        GoodsDeployRelation relation = GoodsDeployRelation.generate(goods, OuterOrderPartner.WB);
        groupbuyInfoParams.put("groupbuyId", String.valueOf(relation.linkId));


        //商家信息参数
        String[] partnerKeys = new String[] {"partnerId","title","shortTitle","telephone","webUrl","busline","mapImg",
                "mapServiceId","mapUrl","latitude","longitude","address","circleId"};
        List<Map<String, String>> partnerParams = new ArrayList<>();
        //将团购信息参数中的特定key值转移出来，构建商家信息参数
        for (int i = 1; i <= shopSize; i++) {
            Map<String, String> partnerParam = new HashMap<>();
            for (String key : partnerKeys) {
                partnerParam.put(key, groupbuyInfoParams.remove(key + "_" + i));
            }
            partnerParams.add(partnerParam);
        }

        //组装两大参数
        Map<String, Object> wubaParams = new HashMap<>();
        wubaParams.put("groupbuyInfo", groupbuyInfoParams);
        wubaParams.put("partners", partnerParams);

        //发起请求
        JsonObject result =  WubaUtil.sendRequest(wubaParams, "emc.groupbuy.addgroupbuy", false);
        String status = result.get("status").getAsString();
        String msg = result.get("msg").getAsString();

        //保存历史
        if ("10000".equals(status)) {
            ResalerProduct product =  ResalerProduct.createProduct(OuterOrderPartner.WB,
                    result.get("data").getAsJsonObject().get("groupbuyId58").getAsLong(),
                    operateUser.id, goods, relation.linkId);
            ResalerProductJournal.createJournal(product, operateUser.id, new Gson().toJson(wubaParams),
                    ResalerProductJournalType.CREATE, "上传商品");
        }

        render("resale/WubaGroupBuyProducts/result.html", msg);
    }
}

