package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.ktv.*;
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
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        renderJSON(gson.toJson(diffResult));
    }
}