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
import models.resale.ResalerFav;
import models.sales.ChannelGoodsInfo;
import models.sales.ChannelGoodsInfoStatus;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import models.sales.GoodsThirdSupport;
import models.sales.Shop;
import models.supplier.Supplier;
import models.wuba.AreaMapping;
import models.wuba.WubaResponse;
import models.wuba.WubaUtil;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String THIRD_URL = "http://t.58.com/";

    public static void prepare(long goodsId) {
        Object categoryArr = getWUBACategory();
        models.sales.Goods goods = models.sales.Goods.findOnSale(goodsId);
        getGoodsItems(goods);

        List<Shop> shopList = Arrays.asList(goods.getShopList().toArray(new Shop[]{}));
        List<AreaMapping> areaMappingList = AreaMapping.findAll();

        JsonArray jsonArray1 = new JsonParser().parse(new Gson().toJson(shopList)).getAsJsonArray();
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < shopList.size(); i++) {
            JsonObject jsonObject = jsonArray1.get(i).getAsJsonObject();
            String areaName = shopList.get(i).getAreaName();
            jsonObject.addProperty("areaName", areaName);
            for (AreaMapping areaMapping : areaMappingList) {
                if (areaMapping.ybqCircleName.equals(areaName)) {
                    jsonObject.addProperty("areaName", areaMapping.wbCircleName);
                    break;
                }
            }

            jsonArray.add(jsonObject);
        }
        renderArgs.put("editShopList", new ArrayList<>());
        Supplier supplier = Supplier.findById(goods.supplierId);
        render(supplier, shopList, jsonArray, categoryArr);
    }

    /**
     * 一天调用一次分类接口
     *
     * @return
     */
    private static Object getWUBACategory() {
        Object categoryArr = Cache.get("wuba_category");
        if (categoryArr == null) {
            WubaResponse categoryData = WubaUtil.sendRequest(null, "emc.groupbuy.find.allprotype", false, false);
            categoryArr = categoryData.data.getAsJsonArray();
            Cache.set("wuba_category", categoryArr.toString(), "1d");
        }
        return categoryArr;
    }

    public static void edit(long goodsId) {
        Resaler resaler = SecureCAS.getResaler();
        if (!Resaler.WUBA_LOGIN_NAME.equals(resaler.loginName)) {
            error("there is nothing you can do");
        }
        Object categoryArr = getWUBACategory();
        Goods goods = Goods.findById(goodsId);

        GoodsThirdSupport support = GoodsThirdSupport.getSupportGoods(goods, OuterOrderPartner.WB);
        if (support == null) {
            getGoodsItems(goods);
        } else {
            getGoodsSupportItems(support, goods);
        }
        renderArgs.put("isEdit", "edit");
        render("WubaProduct/prepare.html", categoryArr);
    }

    /**
     * 修改团购和修改商家
     */
    public static void update(long goodsId, int prodCategory, int isSend, BigDecimal expressMoney, String[] cities,
                              Integer[] cityIds, Integer[] travelCityIds, String startTime, String endTime, String deadline,
                              int successNum, int saleMaxNum, int buyerMaxNum, int buyerMinNum,
                              BigDecimal prodPrice, BigDecimal groupPrice, int isRefund, int shopSize, int prodTypeId) {
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        if (goods == null) {
            error("商品没找到");
            return;
        }
        Resaler user = SecureCAS.getResaler();
        if (!Resaler.WUBA_LOGIN_NAME.equals(user.loginName)) {
            error("user is not 58 resaler");
        }
        ResalerFav resalerFav = ResalerFav.findByGoodsId(user, goodsId);
        if (resalerFav == null) {
            error("no fav found");
        }
        GoodsDeployRelation deployRelation = GoodsDeployRelation.getLast(goods.id, OuterOrderPartner.WB);
        Long linkId = goods.id;
        if (deployRelation != null) {
            linkId = deployRelation.linkId;
        }
        String[] stringParamKeys = new String[]{
                "prodName", "prodDescription", "prodShortName", "prodImg", "mobileDescription",
                "listShortTitle", "specialmessage"
        };

        Map<String, Object> requestMap = new HashMap<>();
        Map<String, Object> groupbuyInfo = new HashMap<>();

        groupbuyInfo.put("groupbuyId", linkId);
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
        groupbuyInfo.put("endTime", endTime);
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
        groupbuyInfo.put("startTime", startTime);
        groupbuyInfo.put("deadline", deadline);
        groupbuyInfo.put("saleMaxNum", saleMaxNum);
        groupbuyInfo.put("groupPrice", groupPrice);
        groupbuyInfo.put("prodPrice", prodPrice);
        groupbuyInfo.put("isRefund", isRefund);


        WubaResponse response = WubaUtil.sendRequest(groupbuyInfo, "emc.groupbuy.editgroupbuyinfo", false);
        requestMap.put("groupbuyInfo", groupbuyInfo);

        List<Map<String, Object>> partners = new ArrayList<>();
        if (response.isOk()) {
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

                response = WubaUtil.sendRequest(partnerMap, "emc.groupbuy.editpartner", true);

                requestMap.put("partner", partnerMap);
                partners.add(partnerMap);
                if (response.isOk()) {
                    render("WubaProduct/result.html", response);
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

        if (response.isOk()) {
            setUrlParams(cities, cityIds, goods, resalerFav.resaler, resalerFav.thirdGroupbuyId);
            resalerFav.save();
            redirect("/58-status/" + goodsId);
        }
        render("WubaProduct/result.html", response);
    }

    /**
     * 新增团购信息
     */
    public static void upload(long goodsId, int prodCategory, int isSend, BigDecimal expressMoney, String[] cities,
                              Integer[] cityIds, Integer[] travelCityIds, String startTime, String endTime, String deadline,
                              int successNum, int saleMaxNum, int buyerMaxNum, int buyerMinNum,
                              BigDecimal prodPrice, BigDecimal groupPrice, int isRefund, int shopSize, int prodTypeId) {
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        if (goods == null) {
            error("商品没找到");
            return;
        }
        Resaler user = SecureCAS.getResaler();
        if (!Resaler.WUBA_LOGIN_NAME.equals(user.loginName)) {
            error("user is not 58 resaler");
        }
        ResalerFav resalerFav = ResalerFav.find("byGoodsAndResalerAndDeleted", goods, user, DeletedStatus.UN_DELETED).first();
        if (resalerFav == null) {
            error("no fav found");
        }
        GoodsDeployRelation goodsDeployRelation = GoodsDeployRelation.generate(goods, OuterOrderPartner.WB);
        Long linkId = goodsDeployRelation.linkId;

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

        groupbuyInfo.put("groupbuyId", linkId);

        groupbuyInfo.put("prodCategory", prodCategory);
        groupbuyInfo.put("isSend", isSend);
        groupbuyInfo.put("expressMoney", expressMoney);
        groupbuyInfo.put("cityIds", cityIds);
        groupbuyInfo.put("travelCityIds", travelCityIds);
        groupbuyInfo.put("startTime", startTime);
        groupbuyInfo.put("endTime", endTime);
        groupbuyInfo.put("deadline", deadline);
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
            String ybqCircleName = request.params.get("ybqCircleName" + i);
            String circleName = request.params.get("circleName" + i);
            if (!circleName.equals(ybqCircleName)) {
                AreaMapping areaMapping = AreaMapping.getArea(ybqCircleName);
                if (areaMapping == null) {
                    new AreaMapping(ybqCircleName, circleName).save();
                } else {
                    areaMapping.wbCircleName = circleName;
                    areaMapping.save();
                }
            }

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
        WubaResponse response = WubaUtil.sendRequest(requestMap, "emc.groupbuy.addgroupbuy", false);

        if (response.isOk()) {
            //成功则保存数据
            GoodsThirdSupport.generate(goods, goodsData, OuterOrderPartner.WB).save();

            Long wubaGoodsId = response.data.getAsJsonObject().get("groupbuyId58").getAsLong();
            setUrlParams(cities, cityIds, goods, resalerFav.resaler, wubaGoodsId);
            resalerFav.partner = OuterOrderPartner.WB;
            resalerFav.lastLinkId = linkId;
            resalerFav.thirdGroupbuyId = wubaGoodsId;
            resalerFav.save();

            redirect("/58-status/" + goodsId);
        }
        render("WubaProduct/result.html", response, goodsId);
    }

    /**
     * 更新商家门店信息
     */
    public static void updateShop(long goodsId, int shopSize) {

        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        if (goods == null) {
            error("商品没找到");
            return;
        }
        Resaler user = SecureCAS.getResaler();
        ResalerFav resalerFav = ResalerFav.findByGoodsId(user, goodsId);
        if (resalerFav == null) {
            error("no fav found");
        }
        Long linkId = resalerFav.lastLinkId;

        Map<String, Object> requestMap = new HashMap<>();
        Map<String, Object> groupbuyInfo = new HashMap<>();
        groupbuyInfo.put("groupbuyId", linkId);

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
            requestMap.put("partners", partners);
        }

        WubaResponse response = WubaUtil.sendRequest(requestMap, "emc.groupbuy.editpartnerbygroupbuy", false);

        if (response.isOk()) {
            render("WubaProduct/result.html", response);
        }
        requestMap.put("partners", partners);
        String goodsData = new Gson().toJson(requestMap);

        //查询是否已经推送过该商品，没有则创建，有则更新
        GoodsThirdSupport support = GoodsThirdSupport.getSupportGoods(goods, OuterOrderPartner.WB);
        if (support == null) {
            GoodsThirdSupport.generate(goods, goodsData, OuterOrderPartner.WB).save();
        } else {
            support.goodsSupplierInfo = goodsData;
            support.save();
        }
        if (response.isOk()) {
            redirect("/58-status/" + goodsId);
        }
        render("WubaProduct/result.html", response, goodsId);

    }

    /**
     * 设置新增或修改成功，显示的参数
     *
     * @param cities
     * @param cityIds
     */
    private static void setUrlParams(String[] cities, Integer[] cityIds, Goods goods, Resaler resaler, Long wubaGoodsId) {
        for (String city : cities) {
            for (int cityId : cityIds) {
                String[] cityArr = city.split(":");
                int city0Id = Integer.valueOf(cityArr[0]);
                if (city0Id == cityId) {
                    String cityName = cityArr[1];
                    String url = THIRD_URL + cityArr[2] + "/" + wubaGoodsId;
                    ChannelGoodsInfo channelGoodsInfo = ChannelGoodsInfo.findByResaler(resaler, url);
                    if (channelGoodsInfo == null) {
                        new ChannelGoodsInfo(goods, resaler, url, cityName, resaler.userName).save();
                    } else {
                        channelGoodsInfo.url = url;
                        channelGoodsInfo.tag = cityName;
                        channelGoodsInfo.status = ChannelGoodsInfoStatus.CREATED;
                        channelGoodsInfo.save();
                    }
                }
            }
        }
    }

    public static void delay(Long goodsId, String deadline) {
        //deadline > endtime
        ResalerFav resalerFav = ResalerFav.findByGoodsId(SecureCAS.getResaler(), goodsId);
        if (resalerFav == null) {
            renderText("bad resalerFav");
        }
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("groupbuyId", String.valueOf(resalerFav.lastLinkId));
        requestMap.put("deadline", deadline);

        WubaResponse response = WubaUtil.sendRequest(requestMap, "emc.groupbuy.delay");
        String msg = "";
        if (response.isOk()) {
            msg = "有效期延长成功！";
        } else {
            msg = response.msg;
        }

        renderText(msg);
    }

    public static void getStatus(Long goodsId) {
        GoodsDeployRelation relation = GoodsDeployRelation.getLast(goodsId, OuterOrderPartner.WB);
        ResalerFav resalerFav = ResalerFav.findByGoodsId(SecureCAS.getResaler(), goodsId);

        if (relation == null) {
            renderText("bad relation");
        }
        if (resalerFav == null) {
            renderText("bad resalerFav");
        }
        Map<String, Object> requestMap = new HashMap<>();
        List<String> ids = new ArrayList<String>();
        ids.add(relation.linkId.toString());
        requestMap.put("groupbuyIds", ids);
        requestMap.put("status", -1);

        WubaResponse response = WubaUtil.sendRequest(requestMap, "emc.groupbuy.getstatus");
        if (!response.isOk()) {
            renderText("failed:" + response);
        }
        JsonArray dataArray = response.data.getAsJsonArray();
        if (!dataArray.isJsonNull()) {
            JsonObject data = dataArray.get(0).getAsJsonObject();
            int statusCode = data.get("status").getAsInt();

            switch (statusCode) {
                case 0:
                    resalerFav.outerStatus = "在售中";
                    break;
                case 1:
                    resalerFav.outerStatus = "审核拒绝";
                    break;
                case 2:
                    resalerFav.outerStatus = "售完下架";
                    break;
                case 3:
                    resalerFav.outerStatus = "强制下架";
                    break;
                default:
                    resalerFav.outerStatus = "未知状态" + statusCode;
            }

        } else {
            resalerFav.outerStatus = "同步失败！";
        }
        resalerFav.save();
        // flash("message" + goodsId, "状态更新成功");
        redirect("/library?goodsId=" + goodsId);
    }


    public static void onsale(Long goodsId) {
        GoodsDeployRelation relation = GoodsDeployRelation.getLast(goodsId, OuterOrderPartner.WB);
        ResalerFav resalerFav = ResalerFav.findByGoodsId(SecureCAS.getResaler(), goodsId);

        if (relation == null) {
            renderText("bad relation");
        }
        if (resalerFav == null) {
            renderText("bad resalerFav");
        }
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("groupbuyId", relation.linkId);
        WubaResponse response = WubaUtil.sendRequest(requestMap, "emc.groupbuy.shangxian");
        if (!response.isOk()) {
            renderText("failed:" + response);
        }
        resalerFav.onsaledAt = new Date();
        resalerFav.save();
        // flash("message" + goodsId, "状态更新成功");
        redirect("/58-status/" + goodsId);
    }


    public static void offsale(Long goodsId) {
        GoodsDeployRelation relation = GoodsDeployRelation.getLast(goodsId, OuterOrderPartner.WB);
        ResalerFav resalerFav = ResalerFav.findByGoodsId(SecureCAS.getResaler(), goodsId);

        if (relation == null) {
            renderText("bad relation");
        }
        if (resalerFav == null) {
            renderText("bad resalerFav");
        }
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("groupbuyId", relation.linkId);
        WubaResponse response = WubaUtil.sendRequest(requestMap, "emc.groupbuy.xiaxian");
        if (!response.isOk()) {
            renderText("failed:" + response);
        }
        resalerFav.offSaleAt = new Date();
        resalerFav.save();
        // flash("message" + goodsId, "状态更新成功");
        redirect("/58-status/" + goodsId);
    }


    private static void getGoodsItems(Goods goods) {
        renderArgs.put("name", goods.name);
        renderArgs.put("title", goods.shortName);
        renderArgs.put("listShortTitle", goods.shortName);
        renderArgs.put("imageLargePath", goods.getImageLargePath());
        renderArgs.put("salePrice", goods.getResalePrice());
        renderArgs.put("faceValue", goods.faceValue);
        renderArgs.put("startTime", DateUtil.getBeginOfDay());
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
        renderArgs.put("isSend", "0");
        renderArgs.put("cityIds", "4");
        renderArgs.put("prodTypeId", "1");
        renderArgs.put("goodsId", goods.id);
    }

    /**
     * 从GoodsThirdSupport读取数据
     *
     * @param support
     */
    private static void getGoodsSupportItems(GoodsThirdSupport support, Goods goods) {
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
                renderArgs.put("groupbuyId", groupbuyInfoAsJsonObject.get("groupbuyId").getAsLong());
                renderArgs.put("prodCategory", groupbuyInfoAsJsonObject.get("prodCategory").getAsInt());
                renderArgs.put("prodTypeId", groupbuyInfoAsJsonObject.get("prodTypeId").getAsInt());
                renderArgs.put("isSend", groupbuyInfoAsJsonObject.get("isSend").getAsString());
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
                    String startTime = groupbuyInfoAsJsonObject.get("startTime").getAsString();
                    renderArgs.put("startTime", DateUtil.stringToDate(startTime, "yyyy-MM-dd"));
                }
                String endTime = groupbuyInfoAsJsonObject.get("endTime").getAsString();

                renderArgs.put("endTime", DateUtil.stringToDate(endTime, DATE_FORMAT));
                String zeroTemp = " 00:00:00";
                String endTemp = " 23:59:59";
                if (endTime.contains(zeroTemp)) {
                    renderArgs.put("endTime", DateUtil.stringToDate(endTime.replace(zeroTemp, endTemp), DATE_FORMAT));
                }
                if (groupbuyInfoAsJsonObject.has("deadline")) {
                    String deadline = groupbuyInfoAsJsonObject.get("deadline").getAsString();
                    renderArgs.put("deadline", DateUtil.stringToDate(deadline, DATE_FORMAT));
                    if (deadline.contains(zeroTemp)) {
                        renderArgs.put("deadline", DateUtil.stringToDate(deadline.replace(zeroTemp, endTemp), DATE_FORMAT));
                    }

                }

                List<Long> cityIds = new ArrayList<>();
                JsonArray jsonArray = groupbuyInfoAsJsonObject.get("cityIds").getAsJsonArray();
                for (JsonElement element : jsonArray) {
                    cityIds.add(element.getAsLong());
                }
                renderArgs.put("cityIds", StringUtils.join(cityIds, ","));
                List<Long> travelCityIds = new ArrayList<>();

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

        List<Shop> shopList = Arrays.asList(goods.getShopList().toArray(new Shop[]{}));
        List<Map<String, Object>> editShopList = new ArrayList<>();

        JsonArray jsonArray = jsonObject.get("partners").getAsJsonArray();
        int shopSize = jsonArray.size();
        if (shopList.size() > 0 && shopList.size() != shopSize) {
            List<AreaMapping> areaMappingList = AreaMapping.findAll();
            Supplier supplier = Supplier.findById(goods.supplierId);
            JsonArray jsonArray1 = new JsonParser().parse(new Gson().toJson(shopList)).getAsJsonArray();
            jsonArray = new JsonArray();
            for (int i = 0; i < shopList.size(); i++) {
                jsonObject = jsonArray1.get(i).getAsJsonObject();
                String areaName = shopList.get(i).getAreaName();
                jsonObject.addProperty("areaName", areaName);
                for (AreaMapping areaMapping : areaMappingList) {
                    if (areaMapping.ybqCircleName.equals(areaName)) {
                        jsonObject.addProperty("areaName", areaMapping.wbCircleName);
                        break;
                    }
                }

                jsonArray.add(jsonObject);
                renderArgs.put("jsonArray", jsonArray);
                renderArgs.put("shopList", shopList);
                renderArgs.put("editShopList", new ArrayList<>());
                renderArgs.put("supplier", supplier);
            }
        } else {
            for (JsonElement element : jsonArray) {
                Map<String, Object> shopMap = new HashMap<>();
                JsonObject partnerObject = element.getAsJsonObject();
                Long partnerId = partnerObject.get("partnerId").getAsLong();
                shopMap.put("name", partnerObject.get("title").getAsString());
                shopMap.put("shortTitle", partnerObject.get("shortTitle").getAsString());
                shopMap.put("partnerId", partnerId);
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
                renderArgs.put("jsonArray", new JsonArray());
                renderArgs.put("editShopList", editShopList);
            }
        }
        renderArgs.put("goodsId", support.goods.id);

    }
}
