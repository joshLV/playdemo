package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.uhuila.common.constants.DeletedStatus;
import controllers.modules.resale.cas.SecureCAS;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.GoodsDeployRelation;
import models.sales.Shop;
import models.supplier.Supplier;
import models.wuba.WubaUtil;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author likang
 *         Date: 12-11-23
 */
@With(SecureCAS.class)
public class WubaProduct extends Controller{

    public static void prepare(long goodsId) {
        Resaler resaler = SecureCAS.getResaler();
        if(!Resaler.WUBA_LOGIN_NAME.equals(resaler.loginName)){
            error("there is nothing you can do");
        }

        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        List<Shop> shopList = Shop.find("bySupplierIdAndDeleted", goods.supplierId, DeletedStatus.UN_DELETED).fetch();

        Supplier supplier = Supplier.findById(goods.supplierId);
        render(goods, supplier, shopList);
    }

    public static void upload(long goodsId, int prodCategory, int isSend, BigDecimal expressMoney,
                              int prodModelType, Integer[] cityIds, Integer[] travelCityIds, Date startTime, Date endTime, Date deadline,
                              int successNum, int saleMaxNum, int buyerMaxNum, int buyerMinNum,
                              BigDecimal prodPrice, BigDecimal groupPrice, int isRefund, int shopSize) {
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        if (goods == null) {
            error("商品没找到"); return;
        }
        GoodsDeployRelation goodsDeployRelation = GoodsDeployRelation.generate(goods, OuterOrderPartner.WUBA);

        String[] stringParamKeys = new String[] {
                "prodName", "prodDescription","prodShortName","prodImg","mobileDescription",
                "listShortTitle","mobileImg","specialmessage"
        };

        Map<String, Object> requestMap = new HashMap<>();
        Map<String, Object> groupbuyInfo = new HashMap<>();
        for(String key : stringParamKeys) {
            groupbuyInfo.put(key, request.params.get(key));
        }

        groupbuyInfo.put("prodTypeId", 1);
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
        groupbuyInfo.put("prodModelType", prodModelType);
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

        for(int i = 1; i <= shopSize; i++) {
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

}
