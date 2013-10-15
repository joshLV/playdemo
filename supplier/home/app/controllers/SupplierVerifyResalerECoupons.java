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
import play.Logger;
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


        if ("1".equals(supplier.getProperty(Supplier.DIAN_PING))) {
            Resaler dpResaler = Resaler.findApprovedByLoginName("dianping");
            List<SupplierResalerShop> resalerShopList = SupplierResalerShop.find("supplier.id=? and resaler=?", supplier.id, dpResaler).fetch();
            renderArgs.put("dpShopList", resalerShopList);
            List<SupplierResalerProduct> resalerProductList = SupplierResalerProduct.find("supplier.id=? and resaler=?", supplier.id, dpResaler).fetch();
            renderArgs.put("dpProductList", resalerProductList);
        }

        if ("1".equals(supplier.getProperty(Supplier.MEI_TUAN))) {
            Resaler mtResaler = Resaler.findApprovedByLoginName("meituan");
            List<SupplierResalerShop> resalerShopList = SupplierResalerShop.find("supplier.id=? and resaler=?", supplier.id, mtResaler).fetch();
            renderArgs.put("resalerShopList", resalerShopList);
            List<SupplierResalerProduct> resalerProductList = SupplierResalerProduct.find("supplier.id=? and resaler=?", supplier.id, mtResaler).fetch();
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

    public static void meituan(String partnerGoodsId, String partnerShopId, List<String> couponIds) {
        if (StringUtils.isBlank(partnerGoodsId)) {
            renderJSON("{\"result\":\"请输入商品信息！\",\"errcode\":1}");
        }
        if (couponIds == null || couponIds.size() == 0) {
            renderJSON("{\"result\":\"请输入券号！\",\"errcode\":1}");
            return;
        }
        SupplierResalerProduct supplierResalerProduct = SupplierResalerProduct.find("partnerGoodsId = ?", partnerGoodsId).first();
        if (supplierResalerProduct == null) {
            renderJSON("[{\"result\":\"输入的美团项目没有指定一百券对应的商品信息\",\"errcode\":1}]}]");
            return;
        }

        Resaler resaler = Resaler.findApprovedByLoginName("meituan");
        SupplierResalerShop supplierResalerShop = SupplierResalerShop.find("resalerPartnerGoodsId=? and resaler=?", partnerGoodsId, resaler).first();

        if (supplierResalerShop == null) {
            renderJSON("[{\"result\":\"输入的美团项目没有指定一百券对应的商品信息\",\"errcode\":1}]}]");
            return;
        }

        if (StringUtils.isBlank(partnerShopId)) {
            partnerShopId = supplierResalerShop.resalerPartnerShopId;
        }
        String cookie = supplierResalerShop.cookieValue;
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
        for (int i = 0; i < couponIds.size(); i++) {
            if (couponIds.get(i).length() != 12) {
                continue;
            }
            params.put("codes[" + i + "]", couponIds.get(i));
        }
        params.put("dealid", partnerGoodsId);
        params.put("bizloginid", partnerShopId);

        Logger.info("美团项目ID：%s,对应门店ID：%s,对应一百券商品ID：%s,对应一百券门店ID：%s", partnerGoodsId, partnerShopId,
                supplierResalerProduct.goods.id.toString(), supplierResalerShop.shop.id);

        WS.HttpResponse response = WS.url("http://e.meituan.com/coupon/batchconsume").params(params).headers(headers).followRedirects(false).post();
        List<Http.Header> headerList = response.getHeaders();
        for (Http.Header header : headerList) {
            System.out.println(header.name + ": " + header.value());
        }

        String body = response.getString();
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("partnerGoodsId", partnerGoodsId);
        jsonMap.put("partnerShopId", partnerShopId);
        jsonMap.put("shopId", supplierResalerShop.shop.id.toString());
        jsonMap.put("goodsId", supplierResalerProduct.goods.id.toString());
//        body = "{\"status\":0,\"data\":[{\"result\":\"\u6d88\u8d39\u6210\u529f\",\"code\":\"9083555383\",\"errcode\":false}]}";
        JsonParser jsonParser = new JsonParser();
        JsonObject result = jsonParser.parse(body).getAsJsonObject();
        String errcode = "";
        if (result.has("data")) {
            JsonArray dataResult = result.get("data").getAsJsonArray();
            for (JsonElement obj : dataResult) {
                JsonObject data = obj.getAsJsonObject();
                String coupon = data.get("code").getAsString();
                errcode = data.get("errcode").getAsString();//成功为false 失败为1
                if (!"1".equals(errcode)) {
                    data.addProperty("goodsname", supplierResalerProduct.partnerGoodsName);
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
            renderJSON(dataResult.toString());
        }

    }

    public static void dianping(List<String> couponIds) {
        Resaler resaler = Resaler.findApprovedByLoginName("dianping");
        SupplierResalerShop supplierResalerShop = SupplierResalerShop.find("resaler=?", resaler).first();
        String cookie = supplierResalerShop.cookieValue;

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Encoding", "gzip,deflate,sdch");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8");
        headers.put("Connection", "keep-alive");
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.116 Safari/537.36");
        headers.put("Host", "e.dianping.com");
        headers.put("Referer", "http://e.dianping.com/account/login/");
        headers.put("Cookie", cookie);

        Map<String, Object> params = new HashMap<>();
        params.put("serialNums", StringUtils.join(couponIds,","));

        params.put("receiptId", 0);
        params.put("t", "m" + System.currentTimeMillis());
        SupplierResalerProduct supplierResalerProduct = SupplierResalerProduct.find("resaler = ? and supplier =? ", resaler,SupplierRbac.currentUser()).first();
        WS.HttpResponse response = WS.url("http://e.dianping.com/tuangou/ajax/batchverify").params(params).headers(headers).followRedirects(false).post();
        String body = response.getString();
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonReponse = jsonParser.parse(body).getAsJsonObject();
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("shopId", supplierResalerShop.shop.id.toString());
        jsonMap.put("goodsId", supplierResalerProduct.goods.id.toString());
        if (jsonReponse.has("msg")) {
            JsonArray dataResult = jsonReponse.get("msg").getAsJsonObject().get("serialNumList").getAsJsonArray();
            for (JsonElement element : dataResult) {
//               element= {"result":{"code":507,"msg":{"message":"验证失败：序列号错误，请重新输入！"}},"serialNum":"3423423523"}
                JsonObject result = element.getAsJsonObject().get("result").getAsJsonObject();
//                result = "code":507,"msg":{"message":"验证失败：序列号错误，请重新输入！"}}
                //成功的情况
                if (!jsonReponse.get("code").getAsString().equals("200")){
                    String coupon = element.getAsJsonObject().get("serialNum").getAsString();
                    OuterOrder outerOrder = OuterOrder.getOuterOrder(coupon, OuterOrderPartner.DP);
                    if (outerOrder == null) {
                        outerOrder = new OuterOrder();
                        outerOrder.resaler = resaler;
                        outerOrder.status = OuterOrderStatus.ORDER_COPY;
                        outerOrder.message=new Gson().toJson(jsonMap);
                        outerOrder.partner = OuterOrderPartner.DP;
                        outerOrder.orderId = coupon;
                        outerOrder.createdAt = new Date();
                        outerOrder.save();
                    }
                }
            }
            renderJSON(dataResult.toString());
        }

    }


}
