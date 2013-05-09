package controllers.resale;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import controllers.OperateRbac;
import models.operator.OperateUser;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.ResalerProduct;
import models.sales.ResalerProductJournal;
import models.sales.ResalerProductJournalType;
import models.sales.ResalerProductStatus;
import models.sales.Shop;
import models.supplier.Supplier;
import models.wuba.WubaResponse;
import models.wuba.WubaUtil;
import operate.rbac.annotations.ActiveNavigation;
import play.Logger;
import play.data.binding.As;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-1-16
 */
@With(OperateRbac.class)
@ActiveNavigation("resale_partner_product")
public class WubaGroupBuyProducts extends Controller {
    private static String[] partnerKeys = new String[] {
            "partnerId","title","shortTitle","telephone","webUrl","busline","mapImg",
            "mapServiceId","mapUrl","latitude","longitude","address","circleId"};

    @ActiveNavigation("resale_partner_product")
    public static void showUpload(Long goodsId) {
        Goods goods = Goods.findById(goodsId);

        String allCategoriesJson = WubaUtil.allProductTypesJsonCache();

        Collection<Shop> shopList = goods.getShopList();
        Supplier supplier = Supplier.findById(goods.supplierId);

        render(goods, allCategoriesJson, shopList, supplier);
    }

    @ActiveNavigation("resale_partner_product")
    public static void upload(long groupbuyId,@As(",") List<String> shopIds, String firstCity) {
        OperateUser operateUser = OperateRbac.currentUser();
        Goods goods = Goods.findById(groupbuyId);
        if (goods == null) {
            notFound();
        }

        //先将所有的参数认为是是团购信息参数
        Map<String, String> groupbuyInfoParams = params.allSimple();
        groupbuyInfoParams.remove("body");
        groupbuyInfoParams.remove("firstCity");
        Resaler wubaResaler = Resaler.findApprovedByLoginName(Resaler.WUBA_LOGIN_NAME);
        ResalerProduct product = ResalerProduct.alloc(OuterOrderPartner.WB, wubaResaler, goods);
        groupbuyInfoParams.put("groupbuyId", String.valueOf(product.goodsLinkId));

        Map<String, Object> prodModelJson = new HashMap<>();
        prodModelJson.put("prodmodcatename", groupbuyInfoParams.get("prodName"));
        prodModelJson.put("prodprice", groupbuyInfoParams.get("prodPrice"));
        prodModelJson.put("groupprice", groupbuyInfoParams.get("groupPrice"));
        prodModelJson.put("prodcode", "");
        prodModelJson.put("count",groupbuyInfoParams.get("saleMaxNum"));
        groupbuyInfoParams.put("prodModelJson", "{" + new Gson().toJson(prodModelJson) + "}");


        //商家信息参数
        List<Map<String, String>> partnerParams = new ArrayList<>();
        //将团购信息参数中的特定key值转移出来，构建商家信息参数
        for (String id : shopIds) {
            Map<String, String> partnerParam = new HashMap<>();
            for (String key : partnerKeys) {
                partnerParam.put(key, groupbuyInfoParams.remove(key + "_" + id));
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
            product.status(ResalerProductStatus.UPLOADED).creator(operateUser.id).save();
            String partnerProductId = response.data.getAsJsonObject().get("groupbuyId58").getAsString();
            product.partnerProduct(partnerProductId).save();
            product.url("http://t.58.com/"+ firstCity + "/"+ partnerProductId);
            product.save();

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
        Collection<Shop> shopList = product.goods.getShopList();
        Goods goods = product.goods;
        String allCategoriesJson = WubaUtil.allProductTypesJsonCache();
        render(product, goods, allCategoriesJson, shopList, supplier);
    }

    @ActiveNavigation("resale_partner_product")
    public static void editGroupBuyInfo(Long productId) {
        ResalerProduct product = ResalerProduct.findById(productId);
        if (product == null) {
            notFound();
        }
        Map<String, String> requestParams = params.allSimple();
        requestParams.remove("body");
        requestParams.remove("productId");

        Map<String, Object> wubaParams = new HashMap<>();
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            wubaParams.put(entry.getKey(), entry.getValue());
        }
        wubaParams.put("groupbuyId", product.goodsLinkId);

        String requestJson = new Gson().toJson(wubaParams);
        Logger.info("wuba editgroupbuyinfo request:\n %s", requestJson);

        //发起请求
        WubaResponse response =  WubaUtil.sendRequest(wubaParams, "emc.groupbuy.editgroupbuyinfo", false);
        //保存历史
        if (response.isOk()) {
            OperateUser operateUser = OperateRbac.currentUser();
            product.lastModifier(operateUser.id).save();
            ResalerProductJournal.createJournal(product, operateUser.id, requestJson,
                    ResalerProductJournalType.UPDATE, "修改团购信息");
        }

        render("resale/WubaGroupBuyProducts/result.html", response);

    }
    @ActiveNavigation("resale_partner_product")
    public static void editPartners(Long productId, @As(",") List<String> shopIds) {
        ResalerProduct product = ResalerProduct.findById(productId);
        if (product == null) {
            notFound();
        }
        Map<String, String> requestParams = params.allSimple();
        requestParams.remove("productId");
        requestParams.remove("shopIds");
        //商家信息参数
        List<Map<String, String>> partnerParams = new ArrayList<>();
        for (String id : shopIds) {
            Map<String, String> partnerParam = new HashMap<>();
            for (String key : partnerKeys) {
                partnerParam.put(key, requestParams.get(key + "_" + id));
            }
            partnerParams.add(partnerParam);
        }

        //外面包一层
        Map<String, Object> wubaParams = new HashMap<>();
        wubaParams.put("partners", partnerParams);
        Map<String, String> groupbuyInfoParams = new HashMap<>();
        groupbuyInfoParams.put("groupbuyId", String.valueOf(product.goodsLinkId));
        wubaParams.put("groupbuyInfo", groupbuyInfoParams);

        //发起请求
        WubaResponse response =  WubaUtil.sendRequest(wubaParams, "emc.groupbuy.editpartnerbygroupbuy", false);

        //保存历史
        if (response.isOk()) {
            OperateUser operateUser = OperateRbac.currentUser();
            product.lastModifier(operateUser.id).save();
            ResalerProductJournal.createJournal(product, operateUser.id, new Gson().toJson(wubaParams),
                    ResalerProductJournalType.UPDATE, "修改商户信息");
        }
        render("resale/WubaGroupBuyProducts/result.html", response);
    }
    @ActiveNavigation("resale_partner_product")
    public static void editDeadline(String endTime, String deadline, Long productId) {
        ResalerProduct product = ResalerProduct.findById(productId);
        if (product == null) {
            notFound();
        }

        //延长券有效期
        Map<String, Object> deadlineRequestMap = new HashMap<>();
        deadlineRequestMap.put("groupbuyId", String.valueOf(product.goodsLinkId));
        deadlineRequestMap.put("deadline", deadline);

        WubaResponse response =  WubaUtil.sendRequest(deadlineRequestMap, "emc.groupbuy.delay", false);
        if (response.isOk()) {
            OperateUser operateUser = OperateRbac.currentUser();
            product.lastModifier(operateUser.id).save();
            ResalerProductJournal.createJournal(product, operateUser.id, new Gson().toJson(deadlineRequestMap),
                    ResalerProductJournalType.UPDATE, "延长券有效期");
            //延长团购有效期
            Map<String, Object> endTimeRequestMap = new HashMap<>();
            endTimeRequestMap.put("groupbuyId", product.goodsLinkId);
            endTimeRequestMap.put("endTime", endTime);

            response = WubaUtil.sendRequest(endTimeRequestMap, "emc.groupbuy.editgroupbuyinfo", false);
            if(response.isOk()) {
                product.lastModifier(operateUser.id).save();
                ResalerProductJournal.createJournal(product, operateUser.id, new Gson().toJson(endTimeRequestMap),
                        ResalerProductJournalType.UPDATE, "延长团购有效期");
            }
        }
        render("resale/WubaGroupBuyProducts/result.html", response);
    }
    @ActiveNavigation("resale_partner_product")
    public static void refreshStatus(Long productId) {
        ResalerProduct product = ResalerProduct.findById(productId);
        if (product == null) {
            notFound();
        }
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("groupbuyIds", "['" + product.goodsLinkId + "']");
        requestMap.put("status", -1);
        WubaResponse response = WubaUtil.sendRequest(requestMap, "emc.groupbuy.getstatus");
        if (!response.isOk()) {
            render("resale/WubaGroupBuyProducts/result.html", response);
        }

        if (response.data.getAsJsonArray().size() > 0) {
            JsonObject data = response.data.getAsJsonArray().get(0).getAsJsonObject();
            int statusCode = data.get("status").getAsInt();
            switch (statusCode) {
                case 0:
                    product.status = ResalerProductStatus.OFFSALE;
                    break;
                case 1:
                    product.status = ResalerProductStatus.REJECTED;
                    break;
                case 2:
                case 3:
                    product.status = ResalerProductStatus.OFFSALE;
                    break;
                default:
                    product.status = ResalerProductStatus.UNKNOWN;
            }
            product.save();
        }
        redirect("/resaler-products/products/wb/" + product.goods.id);
    }

    @ActiveNavigation("resale_partner_product")
    public static void onsale(Long productId) {
        ResalerProduct product = ResalerProduct.findById(productId);
        if (product == null) {
            notFound();
        }
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("groupbuyId", product.goodsLinkId);
        WubaResponse response = WubaUtil.sendRequest(requestMap, "emc.groupbuy.shangxian");
        if (!response.isOk()) {
            render("resale/WubaGroupBuyProducts/result.html", response);
        }
        product.status = ResalerProductStatus.ONSALE;
        product.save();
        redirect("/resaler-products/products/wb/" + product.goods.id);
    }

    @ActiveNavigation("resale_partner_product")
    public static void offsale(Long productId) {
        ResalerProduct product = ResalerProduct.findById(productId);
        if (product == null) {
            notFound();
        }
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("groupbuyId", product.goodsLinkId);
        WubaResponse response = WubaUtil.sendRequest(requestMap, "emc.groupbuy.xiaxian");
        if (!response.isOk()) {
            render("resale/WubaGroupBuyProducts/result.html", response);
        }
        product.status = ResalerProductStatus.OFFSALE;
        product.save();
        redirect("/resaler-products/products/wb/" + product.goods.id);
    }
}
