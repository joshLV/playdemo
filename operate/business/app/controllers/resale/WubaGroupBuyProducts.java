package controllers.resale;

import com.google.gson.Gson;
import controllers.OperateRbac;
import models.operator.OperateUser;
import models.order.OuterOrderPartner;
import models.resale.ResalerProduct;
import models.resale.ResalerProductJournal;
import models.resale.ResalerProductJournalType;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import models.sales.Shop;
import models.supplier.Supplier;
import models.wuba.WubaResponse;
import models.wuba.WubaUtil;
import operate.rbac.annotations.ActiveNavigation;
import play.db.jpa.JPA;
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
        ResalerProduct product = ResalerProduct.generate(operateUser.id, OuterOrderPartner.WB, goods);
        groupbuyInfoParams.put("groupbuyId", String.valueOf(product.id));

        Map<String, Object> prodModelJson = new HashMap<>();
        prodModelJson.put("prodmodcatename", groupbuyInfoParams.get("prodName"));
        prodModelJson.put("prodprice", groupbuyInfoParams.get("prodPrice"));
        prodModelJson.put("groupprice", groupbuyInfoParams.get("groupPrice"));
        prodModelJson.put("prodcode", "");
        prodModelJson.put("count", 0);
        groupbuyInfoParams.put("prodModelJson", "{" + new Gson().toJson(prodModelJson) + "}");


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
        WubaResponse response =  WubaUtil.sendRequest(wubaParams, "emc.groupbuy.addgroupbuy", false);
        //保存历史
        if (response.isOk()) {
            Long partnerProductId = response.data.getAsJsonObject().get("groupbuyId58").getAsLong();
            product.partnerProduct(partnerProductId).save();

            ResalerProductJournal.createJournal(product, operateUser.id, new Gson().toJson(wubaParams),
                    ResalerProductJournalType.CREATE, "上传商品");
        }

        render("resale/WubaGroupBuyProducts/result.html", response);
    }
    @ActiveNavigation("resale_partner_product")
    public static void showEdit(Long productId) {
        ResalerProduct product = ResalerProduct.findById(productId);
        if (product == null) {
            notFound();
        }

        Supplier supplier = Supplier.findById(product.goods.supplierId);
        Set<Shop> shopList = product.goods.shops;
        render(product, shopList, supplier);
    }

    @ActiveNavigation("resale_partner_product")
    public static void editGroupBuyInfo() {

    }
    @ActiveNavigation("resale_partner_product")
    public static void editPartners() {

    }
    @ActiveNavigation("resale_partner_product")
    public static void editDeadline() {

    }
}

