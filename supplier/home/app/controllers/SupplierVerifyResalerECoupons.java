package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import controllers.supplier.SupplierInjector;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.resale.Resaler;
import models.sales.Shop;
import models.sales.SupplierResalerProduct;
import models.sales.SupplierResalerShop;
import models.supplier.Supplier;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: yan
 * Date: 13-7-24
 * Time: 上午10:00
 */
@With({SupplierRbac.class, SupplierInjector.class})
@ActiveNavigation("coupons_multi_index")
public class SupplierVerifyResalerECoupons extends Controller {
    /**
     * 券验证页面
     */
    public static void index() {
        Supplier supplier = SupplierRbac.currentUser().supplier;
        Long supplierUserId = SupplierRbac.currentUser().id;
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        List<Shop> shopList = Shop.findShopBySupplier(supplier.id);
        List<String> verifiedCoupons = ECoupon.getRecentVerified(supplierUser, 5);
        renderArgs.put("verifiedCoupons", verifiedCoupons);

        if (shopList.size() == 0) {
            error("该商户没有添加门店信息！");
        }

        if ("1".equals(supplier.getProperty(Supplier.MEI_TUAN)) || "1".equals(supplier.getProperty(Supplier.DIAN_PING))) {
            List<SupplierResalerShop> resalerShopList = SupplierResalerShop.find("supplier.id=? and resaler=?", supplier.id, Resaler.findApprovedByLoginName("meituan")).fetch();
            renderArgs.put("resalerShopList", resalerShopList);
            List<SupplierResalerProduct> resalerProductList = SupplierResalerProduct.find("supplier.id=? and resaler=?", supplier.id, Resaler.findApprovedByLoginName("meituan")).fetch();
            renderArgs.put("resalerProductList", resalerProductList);
        }

        if (supplierUser.shop == null) {
            render(shopList, supplierUser);
        } else {
            Shop shop = supplierUser.shop;
            //根据页面录入券号查询对应信息
            render(shop, supplierUser);
        }
    }

    public static void meituan(String goodsId, String partnerGoodsId, String partnerShopId, String eCouponSn) {

        String message = "";
        if (StringUtils.isBlank(partnerGoodsId)) {
            message = "请选择美团项目！";
            renderJSON("{\"message\":\"" + message + "\"}");
        }
        if (StringUtils.isBlank(partnerShopId)) {
            message = "请选择门店！";
            renderJSON("{\"message\":\"" + message + "\"}");
        }
        if (eCouponSn == null) {
            message = "请输入券号！";
            renderJSON("{\"message\":\"" + message + "\"}");
        }

        Resaler resaler = Resaler.findApprovedByLoginName("meituan");
        Supplier supplier = SupplierRbac.currentUser().supplier;
        SupplierResalerShop supplierResalerShop = SupplierResalerShop.find("supplier=? and resaler=? and resalerPartnerShopId=?", supplier, resaler, partnerShopId).first();
        String cookie = "";
        if (supplierResalerShop != null) {
            cookie = supplierResalerShop.cookieValue;
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "*/*");
        headers.put("Accept-Encoding", "deflate,sdch");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8");
        headers.put("Cache-Control", "max-age=0");
        headers.put("Connection", "keep-alive");
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.116 Safari/537.36");

        headers.put("Host", "e.meituan.com");
        headers.put("Origin", "http://e.meituan.com");
        headers.put("Referer", "http://e.meituan.com/coupon/");
        headers.put("X-Requested-With", "XMLHttpRequest");

        headers.put("Cookie", cookie);
        Map<String, Object> params = new HashMap<>();
//        for (int i = 0; i < eCouponSns.length; i++) {
        params.put("codes[0]", eCouponSn);
//        }
        params.put("dealid", partnerGoodsId);
        params.put("bizloginid", partnerShopId);
        WS.HttpResponse response = WS.url("http://e.meituan.com/coupon/batchconsume").params(params).headers(headers).followRedirects(false).post();
        List<Http.Header> headerList = response.getHeaders();
        for (Http.Header header : headerList) {
            System.out.println(header.name + ": " + header.value());
        }
        String body = response.getString();
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("partnerGoodsId", partnerGoodsId);
        jsonMap.put("partnerShopId", partnerShopId);
        jsonMap.put("goodsId", goodsId);
//        body = "{\"status\":0,\"data\":[{\"result\":\"\u6d88\u8d39\u6210\u529f\",\"code\":\"201819625306\",\"errcode\":false}]}";
        JsonParser jsonParser = new JsonParser();
        JsonObject result = jsonParser.parse(body).getAsJsonObject();
        if (result.has("data")) {
            JsonArray dataResult = result.get("data").getAsJsonArray();
            System.out.println("data:" + dataResult);
            for (JsonElement obj : dataResult) {
                JsonObject data = obj.getAsJsonObject();
                String coupon = data.get("code").getAsString();
                String errcode = data.get("errcode").getAsString();//成功为false 失败为1
                message = data.get("result").getAsString();
                if (!"1".equals(errcode)) {
                    OuterOrder outerOrder = OuterOrder.getOuterOrder(coupon, OuterOrderPartner.MT);
                    if (outerOrder == null) {
                        outerOrder = new OuterOrder();
                        outerOrder.resaler = resaler;
                        outerOrder.status = OuterOrderStatus.ORDER_COPY;
                        outerOrder.partner = OuterOrderPartner.MT;
                        outerOrder.orderId = coupon;
                        outerOrder.message = new Gson().toJson(jsonMap);
                        outerOrder.createdAt = new Date();
                        outerOrder.save();
                    }

                }
            }
        }
        renderJSON("{\"message\":\"" + message + "\"}");
    }
}
