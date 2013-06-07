package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.taobao.api.ApiException;
import com.taobao.api.Constants;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.ItemGetRequest;
import com.taobao.api.response.ItemGetResponse;
import com.uhuila.common.constants.DeletedStatus;
import models.accounts.AccountType;
import models.ktv.KtvProductGoods;
import models.ktv.KtvRoomType;
import models.ktv.KtvTaobaoSku;
import models.ktv.KtvTaobaoUtil;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.order.OuterOrderPartner;
import models.sales.ResalerProduct;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.libs.Crypto;
import play.mvc.Controller;

import java.util.*;

/**
 * @author likang
 *         Date: 13-6-6
 */
public class TaobaoSkuInfo extends Controller {
    private static final String AES_KEY =  "U9(sDUqXjG2sD&mV";

    public static void stats() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<KtvProductGoods> productGoodsList = KtvProductGoods.all().fetch();
        for (KtvProductGoods productGoods : productGoodsList) {
            //更新该ktv产品所对应的每一个分销渠道上的商品
            List<ResalerProduct> resalerProductList = ResalerProduct.find(
                    "byGoodsAndPartnerAndDeleted",
                    productGoods.goods, OuterOrderPartner.TB, DeletedStatus.UN_DELETED).fetch();


            for(ResalerProduct resalerProduct : resalerProductList) {
                ItemGetRequest request = new ItemGetRequest();
                request.setNumIid(Long.parseLong(resalerProduct.partnerProductId));
                request.setFields("approve_status");

                TaobaoClient taobaoClient = new DefaultTaobaoClient(KtvTaobaoUtil.URL, resalerProduct.resaler.taobaoCouponAppKey,
                        resalerProduct.resaler.taobaoCouponAppSecretKey, Constants.FORMAT_JSON, 15000, 15000);
                //找到淘宝的token
                OAuthToken token = OAuthToken.getOAuthToken(resalerProduct.resaler.id, AccountType.RESALER, WebSite.TAOBAO);

                try {
                    ItemGetResponse response = taobaoClient.execute(request, token.accessToken);
                    if (response.isSuccess()) {
                        Map<String, Object>  objectMap = new HashMap<>();
                        objectMap.put("seller_id", resalerProduct.resaler.taobaoSellerId);
                        objectMap.put("num_iid", request.getNumIid());
                        objectMap.put("approve_status", response.getItem().getApproveStatus());
                        result.add(objectMap);
                    }else {
                        if (response.getSubCode().equals("isv.item-get-service-error:ITEM_NOT_FOUND") ||
                                response.getSubCode().equals("isv.item-is-delete:invalid-numIid")
                                ) {
                            resalerProduct.deleted = DeletedStatus.DELETED;
                            resalerProduct.save();
                        }else {
                            Logger.info("get taobao item status failed: %s %s", resalerProduct.partnerProductId, response.getSubCode());
                        }
                    }
                }catch (ApiException e) {
                    Logger.error("get taobao item status failed " + resalerProduct.partnerProductId);
                }

            }
        }
        renderJSON(result);
    }

    public static void diff(String partnerProductId) {
        ResalerProduct resalerProduct = ResalerProduct.find("byPartnerAndPartnerProductId", OuterOrderPartner.TB, partnerProductId).first();
        KtvProductGoods productGoods =  KtvProductGoods.find("byGoods", resalerProduct.goods).first();


        //构建新的淘宝SKU列表
        SortedMap<KtvRoomType, SortedMap<Date, SortedMap<Integer, KtvTaobaoSku>>> localSkuMap
                = KtvTaobaoUtil.buildTaobaoSku(productGoods.shop, productGoods.product, true);

        if (resalerProduct.resaler == null) {
            Logger.info("reslaer is null");
            error();
        }
        if (StringUtils.isBlank(resalerProduct.partnerProductId)) {
            Logger.info("partner product is null");
            error();
        }
        List<KtvTaobaoSku> remoteSkuList = KtvTaobaoUtil.getTaobaoSku(resalerProduct);


        Map<String, List<KtvTaobaoSku>> diffResult =  KtvTaobaoUtil.diffSkuBetweenLocalAndRemote(localSkuMap, remoteSkuList);

        OAuthToken token = OAuthToken.getOAuthToken(resalerProduct.resaler.id, AccountType.RESALER, WebSite.TAOBAO);

        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, List<KtvTaobaoSku>> entry : diffResult.entrySet()) {
            List<Map<String, Object>> skuList = new ArrayList<>();
            for (KtvTaobaoSku sku : entry.getValue()) {
                Map<String, Object> s = new HashMap<>();
                s.put("roomType", sku.getRoomType());
                s.put("date", sku.getDate());
                s.put("startTime", sku.getStartTime());
                s.put("duration", sku.getDuration());
                s.put("timeRangeCode", sku.getTimeRangeCode());
                s.put("price", sku.getPrice());
                s.put("quantity", sku.getQuantity());
                s.put("taobaoSkuId", sku.getTaobaoSkuId());
                s.put("taobaoOuterIid", sku.getTaobaoOuterIid());
                s.put("taobaoProperties", sku.getTaobaoProperties());
                skuList.add(s);
            }
            result.put(entry.getKey(), skuList);
        }
        result.put("id", Crypto.encryptAES(
                resalerProduct.resaler.taobaoCouponAppKey + ";" +
                        resalerProduct.resaler.taobaoCouponAppSecretKey + ";" +
                        token.accessToken,
                AES_KEY
        ));
        result.put("numIid", partnerProductId);


        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        renderJSON(gson.toJson(result));
    }
}
