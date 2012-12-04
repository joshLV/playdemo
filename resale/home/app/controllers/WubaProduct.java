package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import controllers.modules.resale.cas.SecureCAS;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import models.sales.GoodsThirdSupport;
import models.sales.Shop;
import models.supplier.Supplier;
import models.wuba.WubaUtil;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-11-23
 */
@With(SecureCAS.class)
public class WubaProduct extends Controller {
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static void prepare1(long goodsId) {
        Resaler resaler = SecureCAS.getResaler();
        if (!Resaler.WUBA_LOGIN_NAME.equals(resaler.loginName)) {
            error("there is nothing you can do");
        }

        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        List<Shop> shopList = Shop.find("bySupplierIdAndDeleted", goods.supplierId, DeletedStatus.UN_DELETED).fetch();

        Supplier supplier = Supplier.findById(goods.supplierId);
        render(goods, supplier, shopList);
    }

    public static void prepare(long goodsId) {
        Resaler resaler = SecureCAS.getResaler();
        if (!Resaler.WUBA_LOGIN_NAME.equals(resaler.loginName)) {
            error("there is nothing you can do");
        }

        Goods goods = Goods.findById(goodsId);

        GoodsThirdSupport support = GoodsThirdSupport.getSupportGoods(goods, OuterOrderPartner.WB);
        if (support == null) {
            getGoodsItems(goods);
        } else {
            getGoodsSupportItems(support);
        }

        List<Shop> shopList = Shop.find("bySupplierIdAndDeleted", goods.supplierId, DeletedStatus.UN_DELETED).fetch();

        Supplier supplier = Supplier.findById(goods.supplierId);
        render(supplier, shopList);
    }

    public static void upload(long goodsId, int prodCategory, int isSend, BigDecimal expressMoney,
                              Integer[] cityIds, Integer[] travelCityIds, Date startTime, Date endTime, Date deadline,
                              int successNum, int saleMaxNum, int buyerMaxNum, int buyerMinNum,
                              BigDecimal prodPrice, BigDecimal groupPrice, int isRefund, int shopSize) {
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        if (goods == null) {
            error("商品没找到");
            return;
        }
        GoodsDeployRelation goodsDeployRelation = GoodsDeployRelation.generate(goods, OuterOrderPartner.WB);

        String[] stringParamKeys = new String[]{
                "prodName", "prodDescription", "prodShortName", "prodImg", "mobileDescription",
                "listShortTitle", "mobileImg", "specialmessage"
        };

        Map<String, Object> requestMap = new HashMap<>();
        Map<String, Object> groupbuyInfo = new HashMap<>();
        for (String key : stringParamKeys) {
            groupbuyInfo.put(key, request.params.get(key));
        }

        groupbuyInfo.put("prodTypeId", 1);
        groupbuyInfo.put("prodModelType", 1);
        Map<String, Object> prodModelJson = new HashMap<>();
        prodModelJson.put("prodmodcatename", groupbuyInfo.get("prodName"));
        prodModelJson.put("prodprice", prodPrice);
        prodModelJson.put("groupprice", groupPrice);
        prodModelJson.put("prodcode", "");
        prodModelJson.put("count", 0);
        groupbuyInfo.put("prodModelJson", "{" + new Gson().toJson(prodModelJson) + "}");

        groupbuyInfo.put("groupbuyId", goodsDeployRelation.linkId);

        groupbuyInfo.put("prodCategory", prodCategory);
        groupbuyInfo.put("isSend", isSend);
        groupbuyInfo.put("expressMoney", expressMoney);
        groupbuyInfo.put("cityIds", cityIds);

        groupbuyInfo.put("travelCityIds", travelCityIds);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        groupbuyInfo.put("startTime", simpleDateFormat.format(startTime));
        groupbuyInfo.put("endTime", simpleDateFormat.format(endTime));
        groupbuyInfo.put("deadline", simpleDateFormat.format(deadline));
        groupbuyInfo.put("successNum", successNum);
        groupbuyInfo.put("saleMaxNum", saleMaxNum);
        groupbuyInfo.put("buyerMaxNum", buyerMaxNum);
        groupbuyInfo.put("buyerMinNum", buyerMinNum);
        groupbuyInfo.put("groupPrice", groupPrice);
        groupbuyInfo.put("prodPrice", prodPrice);
        groupbuyInfo.put("isRefund", isRefund);

        requestMap.put("groupbuyInfo", groupbuyInfo);

        List<Map<String, Object>> partners = new ArrayList<>();

        for (int i = 1; i <= shopSize; i++) {
            Map<String, Object> partnerMap = new HashMap<>();
            partnerMap.put("partnerId", Long.parseLong(request.params.get("partnerId_" + i)));
            partnerMap.put("title", request.params.get("title_" + i));
            partnerMap.put("shortTitle", request.params.get("shortTitle_" + i));
            partnerMap.put("circleId", Long.parseLong(request.params.get("circleId_" + i)));
            partnerMap.put("address", request.params.get("address_" + i));
            partnerMap.put("telephone", request.params.get("telephone_" + i));
            partnerMap.put("webUrl", request.params.get("webUrl_" + i));
            partnerMap.put("busline", request.params.get("busline_" + i));
            partnerMap.put("mapImg", request.params.get("mapImg_" + i));
            partnerMap.put("mapUrl", request.params.get("mapUrl_" + i));
            partnerMap.put("mapServiceId", request.params.get("mapServiceId_" + i) == null ? null : Integer.parseInt(request.params.get("mapServiceId_" + i)));
            partnerMap.put("latitude", request.params.get("latitude_" + i));
            partnerMap.put("longitude", request.params.get("longitude_" + i));

            partners.add(partnerMap);

        }
        requestMap.put("partners", partners);

        String goodsData = new Gson().toJson(requestMap);

        //查询是否已经推送过该商品，没有则创建，有则更新
        GoodsThirdSupport support = GoodsThirdSupport.getSupportGoods(goods, OuterOrderPartner.WB);
        if (support == null) {
            GoodsThirdSupport.generate(goods, goodsData, OuterOrderPartner.WB).save();
        } else {
            support.goodsData = goodsData;
            support.save();
        }

        JsonObject result = WubaUtil.sendRequest(requestMap, "emc.groupbuy.addgroupbuy", false);
        String status = result.get("status").getAsString();
        String msg = result.get("msg").getAsString();
        render("WubaProduct/result.html", status, msg);
    }

    public static void upload1(long goodsId, int prodCategory, int isSend, BigDecimal expressMoney,
                               Integer[] cityIds, Integer[] travelCityIds, Date startTime, Date endTime, Date deadline,
                               int successNum, int saleMaxNum, int buyerMaxNum, int buyerMinNum,
                               BigDecimal prodPrice, BigDecimal groupPrice, int isRefund, int shopSize) {
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        if (goods == null) {
            error("商品没找到");
            return;
        }
        GoodsDeployRelation goodsDeployRelation = GoodsDeployRelation.generate(goods, OuterOrderPartner.WB);

        String[] stringParamKeys = new String[]{
                "prodName", "prodDescription", "prodShortName", "prodImg", "mobileDescription",
                "listShortTitle", "mobileImg", "specialmessage"
        };

        Map<String, Object> requestMap = new HashMap<>();
        Map<String, Object> groupbuyInfo = new HashMap<>();
        for (String key : stringParamKeys) {
            groupbuyInfo.put(key, request.params.get(key));
        }

        groupbuyInfo.put("prodTypeId", 1);
        groupbuyInfo.put("prodModelType", 1);
        Map<String, Object> prodModelJson = new HashMap<>();
        prodModelJson.put("prodmodcatename", groupbuyInfo.get("prodName"));
        prodModelJson.put("prodprice", prodPrice);
        prodModelJson.put("groupprice", groupPrice);
        prodModelJson.put("prodcode", "");
        prodModelJson.put("count", 0);
        groupbuyInfo.put("prodModelJson", "{" + new Gson().toJson(prodModelJson) + "}");

        groupbuyInfo.put("groupbuyId", goodsDeployRelation.linkId);

        groupbuyInfo.put("prodCategory", prodCategory);
        groupbuyInfo.put("isSend", isSend);
        groupbuyInfo.put("expressMoney", expressMoney);
        groupbuyInfo.put("cityIds", cityIds);

        groupbuyInfo.put("travelCityIds", travelCityIds);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        groupbuyInfo.put("startTime", simpleDateFormat.format(startTime));
        groupbuyInfo.put("endTime", simpleDateFormat.format(endTime));
        groupbuyInfo.put("deadline", simpleDateFormat.format(deadline));
        groupbuyInfo.put("successNum", successNum);
        groupbuyInfo.put("saleMaxNum", saleMaxNum);
        groupbuyInfo.put("buyerMaxNum", buyerMaxNum);
        groupbuyInfo.put("buyerMinNum", buyerMinNum);
        groupbuyInfo.put("groupPrice", groupPrice);
        groupbuyInfo.put("prodPrice", prodPrice);
        groupbuyInfo.put("isRefund", isRefund);

        requestMap.put("groupbuyInfo", groupbuyInfo);

        List<Map<String, Object>> partners = new ArrayList<>();

        for (int i = 1; i <= shopSize; i++) {
            Map<String, Object> partnerMap = new HashMap<>();
            partnerMap.put("partnerId", Long.parseLong(request.params.get("partnerId_" + i)));
            partnerMap.put("title", request.params.get("title_" + i));
            partnerMap.put("shortTitle", request.params.get("shortTitle_" + i));
            partnerMap.put("circleId", Long.parseLong(request.params.get("circleId_" + i)));
            partnerMap.put("address", request.params.get("address_" + i));
            partnerMap.put("telephone", request.params.get("telephone_" + i));
            partnerMap.put("webUrl", request.params.get("webUrl_" + i));
            partnerMap.put("busline", request.params.get("busline_" + i));
            partnerMap.put("mapImg", request.params.get("mapImg_" + i));
            partnerMap.put("mapUrl", request.params.get("mapUrl_" + i));
            partnerMap.put("mapServiceId", request.params.get("mapServiceId_" + i) == null ? null : Integer.parseInt(request.params.get("mapServiceId_" + i)));
            partnerMap.put("latitude", request.params.get("latitude_" + i));
            partnerMap.put("longitude", request.params.get("longitude_" + i));

            partners.add(partnerMap);

        }
        requestMap.put("partners", partners);


        JsonObject result = WubaUtil.sendRequest(requestMap, "emc.groupbuy.addgroupbuy", false);
        String status = result.get("status").getAsString();
        String msg = result.get("msg").getAsString();
        render("WubaProduct/result.html", status, msg);
    }

    private static void getGoodsItems(Goods goods) {
        renderArgs.put("name", goods.name);
        renderArgs.put("title", goods.title);
        renderArgs.put("listShortTitle", goods.shortName);
        renderArgs.put("imageLargePath", goods.getImageLargePath());
        renderArgs.put("salePrice", goods.getResalePrice());
        renderArgs.put("faceValue", goods.faceValue);
        renderArgs.put("endTime", goods.expireAt);
        renderArgs.put("deadline", goods.expireAt);
        renderArgs.put("exhibition", goods.getExhibition());
        renderArgs.put("prompt", goods.getPrompt());
        renderArgs.put("details", goods.getDetails());
        renderArgs.put("supplierDes", goods.getSupplierDes());
        renderArgs.put("shopList", goods.getShopList());
        renderArgs.put("successNum", "1");
        renderArgs.put("saleMaxNum", "0");
        renderArgs.put("buyerMaxNum", "99");
        renderArgs.put("buyerMinNum", "1");
        renderArgs.put("goodsId", goods.id);
    }

    /**
     * 从GoodsThirdSupport读取数据
     *
     * @param support
     */
    private static void getGoodsSupportItems(GoodsThirdSupport support) {
        JsonElement jsonElement = new JsonParser().parse(support.goodsData);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            if (entry.getKey().equals("groupbuyInfo")) {
                JsonElement groupbuyInfo = entry.getValue();
                JsonObject groupbuyInfoAsJsonObject = groupbuyInfo.getAsJsonObject();
                renderArgs.put("name", groupbuyInfoAsJsonObject.get("prodName").getAsString());
                renderArgs.put("prodDescription", StringUtils.trimToEmpty(groupbuyInfoAsJsonObject.get("prodDescription").getAsString()));
                renderArgs.put("title", groupbuyInfoAsJsonObject.get("prodShortName").getAsString());
                renderArgs.put("imageLargePath", groupbuyInfoAsJsonObject.get("prodImg").getAsString());
                renderArgs.put("mobileDescription", StringUtils.trimToEmpty(groupbuyInfoAsJsonObject.get("mobileDescription").getAsString()));
                renderArgs.put("listShortTitle", StringUtils.trimToEmpty(groupbuyInfoAsJsonObject.get("listShortTitle").getAsString()));
                renderArgs.put("mobileImg", StringUtils.trimToEmpty(groupbuyInfoAsJsonObject.get("mobileImg").getAsString()));
                renderArgs.put("specialmessage", StringUtils.trimToEmpty(groupbuyInfoAsJsonObject.get("specialmessage").getAsString()));
                renderArgs.put("prodCategory", groupbuyInfoAsJsonObject.get("prodCategory").getAsInt());
                renderArgs.put("isSend", groupbuyInfoAsJsonObject.get("isSend").getAsInt());
//                renderArgs.put("expressMoney", groupbuyInfoAsJsonObject.get("expressMoney").getAsDouble());
                renderArgs.put("salePrice", groupbuyInfoAsJsonObject.get("groupPrice").getAsBigDecimal());
                renderArgs.put("faceValue", groupbuyInfoAsJsonObject.get("prodPrice").getAsBigDecimal());
                renderArgs.put("successNum", groupbuyInfoAsJsonObject.get("successNum").getAsString());
                renderArgs.put("saleMaxNum", groupbuyInfoAsJsonObject.get("saleMaxNum").getAsString());
                renderArgs.put("buyerMaxNum", groupbuyInfoAsJsonObject.get("buyerMaxNum").getAsString());
                renderArgs.put("buyerMinNum", groupbuyInfoAsJsonObject.get("buyerMinNum").getAsString());
//                groupbuyInfo.put("prodModelJson", "{" + new Gson().toJson(prodModelJson) + "}");
                renderArgs.put("startTime", DateUtil.stringToDate(groupbuyInfoAsJsonObject.get("startTime").getAsString(), DATE_FORMAT));
                renderArgs.put("endTime", DateUtil.stringToDate(groupbuyInfoAsJsonObject.get("endTime").getAsString(), DATE_FORMAT));
                renderArgs.put("deadline", DateUtil.stringToDate(groupbuyInfoAsJsonObject.get("deadline").getAsString(), DATE_FORMAT));
//                renderArgs.put("cityIds", groupbuyInfoAsJsonObject.get("cityIds").getAsString());
//                renderArgs.put("travelCityIds", groupbuyInfoAsJsonObject.get("travelCityIds").getAsString());
                renderArgs.put("isRefund", groupbuyInfoAsJsonObject.get("isRefund").getAsString());
            }

        }




        renderArgs.put("goodsId", support.goods.id);

    }
}
