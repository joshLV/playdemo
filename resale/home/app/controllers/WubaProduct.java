package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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

    public static void prepare(long goodsId) {
        Resaler resaler = SecureCAS.getResaler();
        if (!Resaler.WUBA_LOGIN_NAME.equals(resaler.loginName)) {
            error("there is nothing you can do");
        }

        models.sales.Goods goods = models.sales.Goods.findById(goodsId);
        getGoodsItems(goods);
        List<Shop> shopList = Shop.find("bySupplierIdAndDeleted", goods.supplierId, DeletedStatus.UN_DELETED).fetch();

        JsonArray jsonArray1 = new JsonParser().parse(new Gson().toJson(shopList)).getAsJsonArray();
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < shopList.size(); i++) {
            JsonObject jsonObject = jsonArray1.get(i).getAsJsonObject();
            jsonObject.addProperty("areaName", shopList.get(i).getAreaName());
            jsonArray.add(jsonObject);
        }
        renderArgs.put("editShopList", new ArrayList<>());
        Supplier supplier = Supplier.findById(goods.supplierId);
        render(supplier, shopList, jsonArray);
    }

    public static void edit(long goodsId) {
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
        renderArgs.put("isEdit", "edit");
        render("WubaProduct/prepare.html");
    }

    public static void update(long goodsId, int prodCategory, int isSend, BigDecimal expressMoney,
                              Integer[] cityIds, Integer[] travelCityIds, Date startTime, Date endTime, Date deadline,
                              int successNum, int saleMaxNum, int buyerMaxNum, int buyerMinNum,
                              BigDecimal prodPrice, BigDecimal groupPrice, int isRefund, int shopSize, int prodTypeId) {
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        if (goods == null) {
            error("商品没找到");
            return;
        }
        GoodsDeployRelation deployRelation = GoodsDeployRelation.find("partner=? and goods=? order by createAt desc", OuterOrderPartner.WB, goods).first();
        Long id = goods.id;
        if (deployRelation != null) {
            id = deployRelation.linkId;
        }
        String[] stringParamKeys = new String[]{
                "prodName", "prodDescription", "prodShortName", "prodImg", "mobileDescription",
                "listShortTitle", "mobileImg,specialmessage"
        };

        Map<String, Object> requestMap = new HashMap<>();
        Map<String, Object> groupbuyInfo = new HashMap<>();

        groupbuyInfo.put("groupbuyId", id);
        groupbuyInfo.put("prodTypeId", prodTypeId);
        groupbuyInfo.put("prodCategory", prodCategory);

        for (String key : stringParamKeys) {
            groupbuyInfo.put(key, request.params.get(key));
        }
        groupbuyInfo.put("isSend", isSend);
        groupbuyInfo.put("expressMoney", expressMoney);
        groupbuyInfo.put("prodModelType", 1);
        groupbuyInfo.put("cityIds", cityIds);
        groupbuyInfo.put("travelCityIds", travelCityIds);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        groupbuyInfo.put("endTime", simpleDateFormat.format(endTime));
        groupbuyInfo.put("successNum", successNum);
        groupbuyInfo.put("buyerMaxNum", buyerMaxNum);
        groupbuyInfo.put("buyerMinNum", buyerMinNum);

        Map<String, Object> prodModelJson = new HashMap<>();
        prodModelJson.put("prodmodcatename", groupbuyInfo.get("prodName"));
        prodModelJson.put("prodprice", prodPrice);
        prodModelJson.put("groupprice", groupPrice);
        prodModelJson.put("prodcode", "");
        prodModelJson.put("count", 0);
        groupbuyInfo.put("prodModelJson", "{" + new Gson().toJson(prodModelJson) + "}");
        groupbuyInfo.put("startTime", simpleDateFormat.format(startTime));
        groupbuyInfo.put("deadline", simpleDateFormat.format(deadline));
        groupbuyInfo.put("saleMaxNum", saleMaxNum);
        groupbuyInfo.put("groupPrice", groupPrice);
        groupbuyInfo.put("prodPrice", prodPrice);
        groupbuyInfo.put("isRefund", isRefund);


        JsonObject result = WubaUtil.sendRequest(groupbuyInfo, "emc.groupbuy.editgroupbuyinfo", false);
        requestMap.put("groupbuyInfo", groupbuyInfo);

        String status = result.get("status").getAsString();
        String msg = result.get("msg").getAsString();
        List<Map<String, Object>> partners = new ArrayList<>();
        if ("10000".equals(status)) {

            for (int i = 1; i <= shopSize; i++) {
                Map<String, Object> partnerMap = new HashMap<>();
                partnerMap.put("partnerId", Long.parseLong(request.params.get("partnerId_" + i)));
                partnerMap.put("title", StringUtils.trimToEmpty(request.params.get("title_" + i)));
                partnerMap.put("shortTitle", StringUtils.trimToEmpty(request.params.get("shortTitle_" + i)));
                partnerMap.put("circleId", Long.parseLong(request.params.get("circleId_" + i)));
                partnerMap.put("address", StringUtils.trimToEmpty(request.params.get("address_" + i)));
                partnerMap.put("telephone", StringUtils.trimToEmpty(request.params.get("telephone_" + i)));
                partnerMap.put("webUrl", StringUtils.trimToEmpty(request.params.get("webUrl_" + i)));
                partnerMap.put("busline", request.params.get("busline_" + i));
                partnerMap.put("mapImg", StringUtils.trimToEmpty(request.params.get("mapImg_" + i)));
                partnerMap.put("mapUrl", StringUtils.trimToEmpty(request.params.get("mapUrl_" + i)));
                partnerMap.put("mapServiceId", request.params.get("mapServiceId_" + i) == null ? null : Integer.parseInt(request.params.get("mapServiceId_" + i)));
                partnerMap.put("latitude", request.params.get("latitude_" + i));
                partnerMap.put("longitude", request.params.get("longitude_" + i));

                result = WubaUtil.sendRequest(partnerMap, "emc.groupbuy.editpartner", false);

                requestMap.put("partner", partnerMap);
                partners.add(partnerMap);
                status = result.get("status").getAsString();
                msg = result.get("msg").getAsString();
                if (!"10000".equals(status)) {
                    render("WubaProduct/result.html", status, msg);
                }
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
        }
        System.out.println();
        render("WubaProduct/result.html", status, msg);
    }

    public static void upload(long goodsId, int prodCategory, int isSend, BigDecimal expressMoney,
                              Integer[] cityIds, Integer[] travelCityIds, Date startTime, Date endTime, Date deadline,
                              int successNum, int saleMaxNum, int buyerMaxNum, int buyerMinNum,
                              BigDecimal prodPrice, BigDecimal groupPrice, int isRefund, int shopSize, int prodTypeId) {
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
        groupbuyInfo.put("prodTypeId", prodTypeId);
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
            partnerMap.put("title", StringUtils.trimToEmpty(request.params.get("title_" + i)));
            partnerMap.put("shortTitle", StringUtils.trimToEmpty(request.params.get("shortTitle_" + i)));
            partnerMap.put("circleId", Long.parseLong(request.params.get("circleId_" + i)));
            partnerMap.put("address", StringUtils.trimToEmpty(request.params.get("address_" + i)));
            partnerMap.put("telephone", StringUtils.trimToEmpty(request.params.get("telephone_" + i)));
            partnerMap.put("webUrl", StringUtils.trimToEmpty(request.params.get("webUrl_" + i)));
            partnerMap.put("busline", request.params.get("busline_" + i));
            partnerMap.put("mapImg", StringUtils.trimToEmpty(request.params.get("mapImg_" + i)));
            partnerMap.put("mapUrl", StringUtils.trimToEmpty(request.params.get("mapUrl_" + i)));
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
        JsonObject result = WubaUtil.sendRequest(requestMap, "emc.groupbuy.editgroupbuyinfo", false);
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
        renderArgs.put("cityIds", "4");

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
                if (groupbuyInfoAsJsonObject.has("mobileImg")) {
                    renderArgs.put("mobileImg", StringUtils.trimToEmpty(groupbuyInfoAsJsonObject.get("mobileImg").getAsString()));
                }
                if (groupbuyInfoAsJsonObject.has("specialmessage")) {
                    renderArgs.put("specialmessage", StringUtils.trimToEmpty(groupbuyInfoAsJsonObject.get("specialmessage").getAsString()));
                }
                renderArgs.put("prodCategory", groupbuyInfoAsJsonObject.get("prodCategory").getAsInt());
                renderArgs.put("prodTypeId", groupbuyInfoAsJsonObject.get("prodTypeId").getAsInt());
                renderArgs.put("isSend", groupbuyInfoAsJsonObject.get("isSend").getAsInt());
                if (groupbuyInfoAsJsonObject.has("expressMoney")) {
                    renderArgs.put("expressMoney", groupbuyInfoAsJsonObject.get("expressMoney").getAsDouble());
                }
                if (groupbuyInfoAsJsonObject.has("groupPrice")) {
                    renderArgs.put("salePrice", groupbuyInfoAsJsonObject.get("groupPrice").getAsBigDecimal());
                }
                if (groupbuyInfoAsJsonObject.has("prodPrice")) {
                    renderArgs.put("faceValue", groupbuyInfoAsJsonObject.get("prodPrice").getAsBigDecimal());
                }
                if (groupbuyInfoAsJsonObject.has("saleMaxNum")) {
                    renderArgs.put("saleMaxNum", groupbuyInfoAsJsonObject.get("saleMaxNum").getAsString());
                }
                renderArgs.put("successNum", groupbuyInfoAsJsonObject.get("successNum").getAsString());

                renderArgs.put("buyerMaxNum", groupbuyInfoAsJsonObject.get("buyerMaxNum").getAsString());
                renderArgs.put("buyerMinNum", groupbuyInfoAsJsonObject.get("buyerMinNum").getAsString());
                if (groupbuyInfoAsJsonObject.has("startTime")) {
                    renderArgs.put("startTime", DateUtil.stringToDate(groupbuyInfoAsJsonObject.get("startTime").getAsString(), DATE_FORMAT));
                }
                renderArgs.put("endTime", DateUtil.stringToDate(groupbuyInfoAsJsonObject.get("endTime").getAsString(), DATE_FORMAT));
                if (groupbuyInfoAsJsonObject.has("deadline")) {
                    renderArgs.put("deadline", DateUtil.stringToDate(groupbuyInfoAsJsonObject.get("deadline").getAsString(), DATE_FORMAT));
                }
                List<Long> cityIds = new ArrayList<>();//
                JsonArray jsonArray = groupbuyInfoAsJsonObject.get("cityIds").getAsJsonArray();
                for (JsonElement element : jsonArray) {
                    cityIds.add(element.getAsLong());
                }
                renderArgs.put("cityIds", StringUtils.join(cityIds, ","));
                List<Long> travelCityIds = new ArrayList<>();//

                if (groupbuyInfoAsJsonObject.has("travelCityIds")) {
                    JsonArray travelArray = groupbuyInfoAsJsonObject.get("travelCityIds").getAsJsonArray();
                    for (JsonElement element : travelArray) {
                        travelCityIds.add(element.getAsLong());
                    }
                    renderArgs.put("travelCityIds", StringUtils.join(travelCityIds, ","));
                }
                if (groupbuyInfoAsJsonObject.has("isRefund")) {
                    renderArgs.put("isRefund", groupbuyInfoAsJsonObject.get("isRefund").getAsString());
                }
            }

        }
        Supplier supplier = Supplier.findById(support.goods.supplierId);
        renderArgs.put("supplier", supplier);

        List<Map<String, Object>> editShopList = new ArrayList<>();

        JsonArray jsonArray = jsonObject.get("partners").getAsJsonArray();
        for (JsonElement element : jsonArray) {
            Map<String, Object> shopMap = new HashMap<>();
            JsonObject partnerObject = element.getAsJsonObject();
            shopMap.put("name", partnerObject.get("title").getAsString());
            shopMap.put("partnerId", partnerObject.get("partnerId").getAsLong());
            shopMap.put("circleId", partnerObject.get("circleId").getAsLong());
            shopMap.put("address", partnerObject.get("address").getAsString());
            shopMap.put("phone", partnerObject.get("telephone").getAsString());
            shopMap.put("latitude", partnerObject.get("latitude").getAsString());
            shopMap.put("longitude", partnerObject.get("longitude").getAsString());
            shopMap.put("webUrl", partnerObject.get("webUrl").getAsString());
            shopMap.put("busline", partnerObject.get("busline").getAsString());
            shopMap.put("mapImg", partnerObject.get("mapImg").getAsString());
            shopMap.put("mapUrl", partnerObject.get("mapUrl").getAsString());
            int mapServiceId = partnerObject.get("mapServiceId").getAsInt();
            shopMap.put("mapServiceId", mapServiceId);
            editShopList.add(shopMap);
        }

        renderArgs.put("jsonArray", new JsonArray());
        renderArgs.put("editShopList", editShopList);
        renderArgs.put("goodsId", support.goods.id);

    }
}
