package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.accounts.AccountType;
import models.ktv.*;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.order.OuterOrderPartner;
import models.sales.ResalerProduct;
import org.apache.commons.lang.StringUtils;
import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    public static void index() {
        render();
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
        result.put("token", token.accessToken);
        result.put("numIid", partnerProductId);

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        renderJSON(gson.toJson(result));
    }
}