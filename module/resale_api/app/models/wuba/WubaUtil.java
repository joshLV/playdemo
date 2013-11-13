package models.wuba;

import cache.CacheCallBack;
import cache.CacheHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.order.ChannelAccountCheckingDetail;
import models.order.ECoupon;
import models.order.ECouponPartner;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.exceptions.UnexpectedException;
import play.libs.Codec;
import util.extension.ExtensionResult;
import util.ws.WebServiceRequest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-11-23
 */
public class WubaUtil {
    public static final String GATEWAY_URL = Play.configuration.getProperty("wuba.gateway_url");
    public static final String WUBA_APP_KEY = Play.configuration.getProperty("wuba.wuba_app_key");
    public static final String YBQ_APP_KEY = Play.configuration.getProperty("wuba.ybq_app_key");
    public static final String SECRET_KEY = Play.configuration.getProperty("wuba.secret_key");
    public static final String PARTNER_ID = Play.configuration.getProperty("wuba.partner_id");

    public static final String CODE_CHARSET = "utf-8";
    public static final String CODE_TRANSFORMATION = "DES/ECB/PKCS5Padding";
    private static final String CACHE_KEY = "WUBA_GROUPBUY_API";

    private static final boolean USE_POST_METHOD = true;
    private static final boolean USE_GET_METHOD = false;

    /**
     * 在58上验证.
     * 在58那边，如果券是已经验证掉的，他们会直接返回成功.
     *
     * @param coupon 一百券自家券
     * @return 是否验证成功
     */
    public static ExtensionResult verifyOnWuba(ECoupon coupon) {
        if (coupon.partner != ECouponPartner.WB) {
            return ExtensionResult.INVALID_CALL;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("ticketId", coupon.id);
        params.put("orderId", coupon.order.orderNumber);
        params.put("ticketIdIndex", 0);
        WubaResponse response = sendRequest(params, "emc.groupbuy.order.ticketcheck");
        if (response.isOk()) {
            JsonObject data = response.data.getAsJsonObject();
            int resultCode = data.get("result").getAsInt();
            switch (resultCode) {
                // 1 已使用 10 未使 用退款, 11 已过 期退款, 12 已消 费退款 (58 责 任),13 已消费退 款(商家 责任) 99 用户 锁定
                case 1:
                    return ExtensionResult.SUCCESS;
                case 10:
                    return ExtensionResult.code(110).message("已退款（未使用）");
                case 11:
                    return ExtensionResult.code(111).message("已退款（券过期）");
                case 12:
                    return ExtensionResult.code(112).message("已退款（已消费-58责任）");
                case 13:
                    return ExtensionResult.code(113).message("已退款（已消费-商家责任）");
                case 99:
                    return ExtensionResult.code(110).message("用户锁定");
                default:
                    return ExtensionResult.code(resultCode).message("未知58同城接口返回代码: %d", resultCode);
            }
        }
        return ExtensionResult.code(1).message("58同城接口调用失败");
    }


    public static List<ChannelAccountCheckingDetail> createWubaAccountCheckingDetail(String date) {
        List<ChannelAccountCheckingDetail> thirdBills = new ArrayList<>();
        ChannelAccountCheckingDetail thirdBillSequence;
        //结算数据查询
        Map<String, Object> params = new HashMap<>();
        params.put("jiesuantime", date);
        WubaResponse response = sendRequest(params, "emc.groupbuy.queryjiesuan", false, false, USE_GET_METHOD);
        JsonObject jsonObject;
        JsonArray jsonArray;
        if (response.isOk()) {
            jsonObject = response.data.getAsJsonObject();
            jsonArray = jsonObject.get("jiesuanDetails").getAsJsonArray();

            //用于本地测试  2013-05-09
//        String jsonStr = "[{\"commission\":0.59,\"groupid3\":26851,\"groupid58\":70096088880131005,\"groupprice\":19.8,\"jiesuanmoney\":19.21,\"orderid3\":81870247,\"orderid58\":70187872236548005,\"paytime\":\"2013-04-05 15:23:23\",\"refundable\":0,\"ticketid58\":70187931446277005,\"type\":\"蛋糕甜品\",\"usetime\":\"2013-05-06 13:46:55\"},{\"commission\":0.59,\"groupid3\":26851,\"groupid58\":70096088880131005,\"groupprice\":19.8,\"jiesuanmoney\":19.21,\"orderid3\":81870247,\"orderid58\":70187872236548005,\"paytime\":\"2013-04-05 15:23:23\",\"refundable\":0,\"ticketid58\":70187931598338005,\"type\":\"蛋糕甜品\",\"usetime\":\"2013-05-06 13:47:13\"},{\"commission\":0.59,\"groupid3\":26851,\"groupid58\":70096088880131005,\"groupprice\":19.8,\"jiesuanmoney\":19.21,\"orderid3\":81870247,\"orderid58\":70187872236548005,\"paytime\":\"2013-04-05 15:23:23\",\"refundable\":0,\"ticketid58\":70187931616260005,\"type\":\"蛋糕甜品\",\"usetime\":\"2013-05-06 13:47:30\"},{\"commission\":0.59,\"groupid3\":26851,\"groupid58\":70096088880131005,\"groupprice\":19.8,\"jiesuanmoney\":19.21,\"orderid3\":10453991,\"orderid58\":70244350536196005,\"paytime\":\"2013-04-06 22:01:26\",\"refundable\":0,\"ticketid58\":70244396211205005,\"type\":\"蛋糕甜品\",\"usetime\":\"2013-05-06 09:34:22\"},{\"commission\":0.59,\"groupid3\":26851,\"groupid58\":70096088880131005,\"groupprice\":19.8,\"jiesuanmoney\":19.21,\"orderid3\":10453991,\"orderid58\":70244350536196005,\"paytime\":\"2013-04-06 22:01:26\",\"refundable\":0,\"ticketid58\":70244396218885005,\"type\":\"蛋糕甜品\",\"usetime\":\"2013-05-06 09:34:06\"},{\"commission\":0.59,\"groupid3\":26851,\"groupid58\":70096088880131005,\"groupprice\":19.8,\"jiesuanmoney\":19.21,\"orderid3\":28625270,\"orderid58\":70271089696258005,\"paytime\":\"2013-04-07 12:31:28\",\"refundable\":0,\"ticketid58\":70271123972610005,\"type\":\"蛋糕甜品\",\"usetime\":\"2013-05-06 09:19:46\"},{\"commission\":10.08,\"groupid3\":14151,\"groupid58\":65846321280517008,\"groupprice\":336,\"jiesuanmoney\":325.92,\"orderid3\":11694587,\"orderid58\":70601292751362008,\"paytime\":\"2013-04-14 23:41:35\",\"refundable\":0,\"ticketid58\":70601367787522008,\"type\":\"美发\",\"usetime\":\"2013-05-06 15:46:12\"},{\"commission\":2.34,\"groupid3\":26850,\"groupid58\":70096062316547009,\"groupprice\":78,\"jiesuanmoney\":75.66,\"orderid3\":54295771,\"orderid58\":70758358738947009,\"paytime\":\"2013-04-18 12:53:52\",\"refundable\":0,\"ticketid58\":70758416948227009,\"type\":\"蛋糕甜品\",\"usetime\":\"2013-05-06 16:20:27\"},{\"commission\":1.74,\"groupid3\":16273,\"groupid58\":67576185662468009,\"groupprice\":58,\"jiesuanmoney\":56.26,\"orderid3\":56754464,\"orderid58\":70773039842818009,\"paytime\":\"2013-04-18 20:51:46\",\"refundable\":0,\"ticketid58\":70773097570818009,\"type\":\"蛋糕甜品\",\"usetime\":\"2013-05-06 14:15:47\"},{\"commission\":0.59,\"groupid3\":26851,\"groupid58\":70096088880131005,\"groupprice\":19.8,\"jiesuanmoney\":19.21,\"orderid3\":37060337,\"orderid58\":71027729704450005,\"paytime\":\"2013-04-24 15:04:01\",\"refundable\":0,\"ticketid58\":71027835575301005,\"type\":\"蛋糕甜品\",\"usetime\":\"2013-05-06 13:31:57\"},{\"commission\":1.65,\"groupid3\":16001,\"groupid58\":67090037884932006,\"groupprice\":55,\"jiesuanmoney\":53.35,\"orderid3\":56569401,\"orderid58\":71125570682370006,\"paytime\":\"2013-04-26 20:07:54\",\"refundable\":0,\"ticketid58\":71125644451333006,\"type\":\"其他美食\",\"usetime\":\"2013-05-06 09:59:36\"},{\"commission\":2.55,\"groupid3\":13921,\"groupid58\":65460328362500006,\"groupprice\":85,\"jiesuanmoney\":82.45,\"orderid3\":41131462,\"orderid58\":71418947241987006,\"paytime\":\"2013-05-03 11:16:33\",\"refundable\":0,\"ticketid58\":71418979205636006,\"type\":\"西餐\",\"usetime\":\"2013-05-06 12:36:28\"},{\"commission\":0.75,\"groupid3\":26142,\"groupid58\":69249590955011007,\"groupprice\":25,\"jiesuanmoney\":24.25,\"orderid3\":60685019,\"orderid58\":71511156164106007,\"paytime\":\"2013-05-05 13:18:08\",\"refundable\":0,\"ticketid58\":71511188008453007,\"type\":\"蛋糕甜品\",\"usetime\":\"2013-05-06 19:37:39\"},{\"commission\":2.07,\"groupid3\":26379,\"groupid58\":69516595100675007,\"groupprice\":69,\"jiesuanmoney\":66.93,\"orderid3\":65696142,\"orderid58\":71515917758467007,\"paytime\":\"2013-05-05 15:53:18\",\"refundable\":0,\"ticketid58\":71515954411010007,\"type\":\"KTV\",\"usetime\":\"2013-05-06 18:07:06\"},{\"commission\":2.97,\"groupid3\":27085,\"groupid58\":70443880389634007,\"groupprice\":99,\"jiesuanmoney\":96.03,\"orderid3\":69113177,\"orderid58\":71516158514190007,\"paytime\":\"2013-05-05 16:01:54\",\"refundable\":0,\"ticketid58\":71516218660363007,\"type\":\"中餐\",\"usetime\":\"2013-05-06 18:02:36\"},{\"commission\":2.64,\"groupid3\":26776,\"groupid58\":70047768546820008,\"groupprice\":88,\"jiesuanmoney\":85.36,\"orderid3\":75720706,\"orderid58\":71546813478916008,\"paytime\":\"2013-05-06 08:39:44\",\"refundable\":0,\"ticketid58\":71546871772677008,\"type\":\"运动健身\",\"usetime\":\"2013-05-06 10:17:21\"},{\"commission\":2.64,\"groupid3\":26776,\"groupid58\":70047768546820008,\"groupprice\":88,\"jiesuanmoney\":85.36,\"orderid3\":75720706,\"orderid58\":71546813478916008,\"paytime\":\"2013-05-06 08:39:44\",\"refundable\":0,\"ticketid58\":71546871832580008,\"type\":\"运动健身\",\"usetime\":\"2013-05-06 10:17:14\"},{\"commission\":1.47,\"groupid3\":15629,\"groupid58\":66958463893509009,\"groupprice\":49,\"jiesuanmoney\":47.53,\"orderid3\":70536378,\"orderid58\":71549010774019009,\"paytime\":\"2013-05-06 09:50:06\",\"refundable\":0,\"ticketid58\":71549033893379009,\"type\":\"蛋糕甜品\",\"usetime\":\"2013-05-06 16:18:03\"},{\"commission\":1.74,\"groupid3\":15800,\"groupid58\":67000077359619005,\"groupprice\":58,\"jiesuanmoney\":56.26,\"orderid3\":76562363,\"orderid58\":71550070262789005,\"paytime\":\"2013-05-06 10:25:06\",\"refundable\":0,\"ticketid58\":71550108910595005,\"type\":\"中餐\",\"usetime\":\"2013-05-06 11:40:40\"},{\"commission\":2.64,\"groupid3\":26776,\"groupid58\":70047768546820008,\"groupprice\":88,\"jiesuanmoney\":85.36,\"orderid3\":73444521,\"orderid58\":71553781765635008,\"paytime\":\"2013-05-06 12:26:47\",\"refundable\":0,\"ticketid58\":71553846893573008,\"type\":\"运动健身\",\"usetime\":\"2013-05-06 12:31:53\"},{\"commission\":13.44,\"groupid3\":27096,\"groupid58\":70445421404677008,\"groupprice\":448,\"jiesuanmoney\":434.56,\"orderid3\":79464324,\"orderid58\":71556528805378008,\"paytime\":\"2013-05-06 13:55:48\",\"refundable\":0,\"ticketid58\":71556581756931008,\"type\":\"体检保健\",\"usetime\":\"2013-05-06 14:15:07\"},{\"commission\":13.44,\"groupid3\":27096,\"groupid58\":70445421404677008,\"groupprice\":448,\"jiesuanmoney\":434.56,\"orderid3\":79464324,\"orderid58\":71556528805378008,\"paytime\":\"2013-05-06 13:55:48\",\"refundable\":0,\"ticketid58\":71556581790734008,\"type\":\"体检保健\",\"usetime\":\"2013-05-06 14:14:39\"},{\"commission\":13.44,\"groupid3\":27096,\"groupid58\":70445421404677008,\"groupprice\":448,\"jiesuanmoney\":434.56,\"orderid3\":79464324,\"orderid58\":71556528805378008,\"paytime\":\"2013-05-06 13:55:48\",\"refundable\":0,\"ticketid58\":71556581796356008,\"type\":\"体检保健\",\"usetime\":\"2013-05-06 14:14:26\"},{\"commission\":3.39,\"groupid3\":27621,\"groupid58\":70975979332613008,\"groupprice\":113,\"jiesuanmoney\":109.61,\"orderid3\":77476945,\"orderid58\":71560819025923008,\"paytime\":\"2013-05-06 16:19:40\",\"refundable\":0,\"ticketid58\":71561001436165008,\"type\":\"景点门票\",\"usetime\":\"2013-05-06 16:25:16\"},{\"commission\":3.39,\"groupid3\":27621,\"groupid58\":70975979332613008,\"groupprice\":113,\"jiesuanmoney\":109.61,\"orderid3\":77476945,\"orderid58\":71560819025923008,\"paytime\":\"2013-05-06 16:19:40\",\"refundable\":0,\"ticketid58\":71561001587714008,\"type\":\"景点门票\",\"usetime\":\"2013-05-06 16:25:16\"},{\"commission\":3.39,\"groupid3\":27621,\"groupid58\":70975979332613008,\"groupprice\":113,\"jiesuanmoney\":109.61,\"orderid3\":77476945,\"orderid58\":71560819025923008,\"paytime\":\"2013-05-06 16:19:40\",\"refundable\":0,\"ticketid58\":71561001608708008,\"type\":\"景点门票\",\"usetime\":\"2013-05-06 16:25:16\"},{\"commission\":5.97,\"groupid3\":15606,\"groupid58\":66955402637315007,\"groupprice\":199,\"jiesuanmoney\":193.03,\"orderid3\":77598428,\"orderid58\":71561340680201007,\"paytime\":\"2013-05-06 16:32:29\",\"refundable\":0,\"ticketid58\":71561394979337007,\"type\":\"东南亚菜\",\"usetime\":\"2013-05-06 19:36:47\"},{\"commission\":2.55,\"groupid3\":15935,\"groupid58\":67049784758274005,\"groupprice\":85,\"jiesuanmoney\":82.45,\"orderid3\":75673661,\"orderid58\":71565189551619005,\"paytime\":\"2013-05-06 18:42:21\",\"refundable\":1,\"ticketid58\":71565383989765005,\"type\":\"蛋糕甜品\",\"usetime\":\"2013-05-06 18:48:21\"},{\"commission\":2.55,\"groupid3\":15935,\"groupid58\":67049784758274005,\"groupprice\":85,\"jiesuanmoney\":82.45,\"orderid3\":75673661,\"orderid58\":71565189551619005,\"paytime\":\"2013-05-06 18:42:21\",\"refundable\":1,\"ticketid58\":71565384196100005,\"type\":\"蛋糕甜品\",\"usetime\":\"2013-05-06 18:48:21\"},{\"commission\":2.55,\"groupid3\":15935,\"groupid58\":67049784758274005,\"groupprice\":85,\"jiesuanmoney\":82.45,\"orderid3\":73502209,\"orderid58\":71572544667150005,\"paytime\":\"2013-05-06 22:38:26\",\"refundable\":1,\"ticketid58\":71572636976133005,\"type\":\"蛋糕甜品\",\"usetime\":\"2013-05-06 22:44:04\"},{\"commission\":2.55,\"groupid3\":15935,\"groupid58\":67049784758274005,\"groupprice\":85,\"jiesuanmoney\":82.45,\"orderid3\":73502209,\"orderid58\":71572544667150005,\"paytime\":\"2013-05-06 22:38:26\",\"refundable\":1,\"ticketid58\":71572637239300005,\"type\":\"蛋糕甜品\",\"usetime\":\"2013-05-06 22:44:03\"}] ";
//        JsonParser jsonParser = new JsonParser();
//        JsonArray jsonArray = jsonParser.parse(jsonStr).getAsJsonArray();
            List<String> ids = new ArrayList<>();
            for (JsonElement element : jsonArray) {
                jsonObject = element.getAsJsonObject();
                String accountedAtStr = "";
                if (jsonObject.get("usetime").isJsonNull()) {
                    accountedAtStr="可能未结算的"+jsonObject.get("orderid58").getAsString();
                } else {
                    accountedAtStr = jsonObject.get("usetime").getAsString();
                }
                String outerOrderNo = jsonObject.get("orderid58").getAsString();
                BigDecimal businessAmount = jsonObject.get("groupprice").getAsBigDecimal();
                BigDecimal commissionFee = jsonObject.get("commission").getAsBigDecimal();
                BigDecimal settleAmount = jsonObject.get("jiesuanmoney").getAsBigDecimal();
                thirdBillSequence = new ChannelAccountCheckingDetail();
                thirdBillSequence.accountedAtStr = accountedAtStr;
                thirdBillSequence.outerOrderNo = outerOrderNo;
                thirdBillSequence.businessAmount = businessAmount;
                thirdBillSequence.commissionFee = commissionFee;
                thirdBillSequence.settleAmount = settleAmount;
                thirdBills.add(thirdBillSequence);
            }
        }
        //结算退款追回数据查询
        params = new HashMap<>();
        params.put("refundtime", date);
        response = sendRequest(params, "emc.groupbuy.queryrefundjiesuan", false, false, USE_GET_METHOD);
        if (response.isOk()) {
            jsonObject = response.data.getAsJsonObject();
            jsonArray = jsonObject.get("refundDetails").getAsJsonArray();
            //用于本地测试  2013-05-09
//            jsonStr = "[{\"commission\":0,\"groupid3\":16031,\"groupid58\":67271546111491009,\"groupprice\":90,\"jiesuanmoney\":90,\"orderid3\":61416174,\"orderid58\":68329269900805009,\"paytime\":\"2013-02-22 15:02:09\",\"refundmoney\":90,\"refundtime\":\"2013-05-09 15:24:05\",\"refundtype\":\"未使用退款\",\"ticketid58\":68329333739525009,\"type\":\"自助餐\",\"usetime\":null},{\"commission\":0,\"groupid3\":16031,\"groupid58\":67271546111491009,\"groupprice\":90,\"jiesuanmoney\":90,\"orderid3\":61416174,\"orderid58\":68329269900805009,\"paytime\":\"2013-02-22 15:02:09\",\"refundmoney\":90,\"refundtime\":\"2013-05-09 15:24:05\",\"refundtype\":\"未使用退款\",\"ticketid58\":68329333751301009,\"type\":\"自助餐\",\"usetime\":null}]";
//            jsonParser = new JsonParser();
//            jsonArray = jsonParser.parse(jsonStr).getAsJsonArray();
            for (JsonElement element : jsonArray) {
                jsonObject = element.getAsJsonObject();
                if (jsonObject.get("refundtype").getAsString().contains("未使用退款")) {
                    continue;
                }
                String accountedAtStr = jsonObject.get("refundtime").getAsString();
                String outerOrderNo = jsonObject.get("orderid58").getAsString();
                BigDecimal businessAmount = jsonObject.get("groupprice").getAsBigDecimal();
                BigDecimal commissionFee = jsonObject.get("commission").getAsBigDecimal();
                BigDecimal settleAmount = jsonObject.get("jiesuanmoney").getAsBigDecimal();
                thirdBillSequence = new ChannelAccountCheckingDetail();
                thirdBillSequence.accountedAtStr = accountedAtStr;
                thirdBillSequence.outerOrderNo = outerOrderNo;
                thirdBillSequence.businessAmount = BigDecimal.ZERO.subtract(businessAmount);
                thirdBillSequence.commissionFee = BigDecimal.ZERO.subtract(commissionFee);
                thirdBillSequence.settleAmount = BigDecimal.ZERO.subtract(settleAmount);
                thirdBills.add(thirdBillSequence);
            }
        }
        return thirdBills;
    }

    public static String consumedBill(String date) {
        //结算数据查询
        JsonObject jsonObject = new JsonObject();
        Map<String, Object> params = new HashMap<>();
        params.put("jiesuantime", date);
        String response = sendRequest1(params, "emc.groupbuy.queryjiesuan", false, false, USE_GET_METHOD);
//        JsonArray jsonArray = new JsonArray();
//        if (response.isOk()) {
//            jsonObject = response.data.getAsJsonObject();
//            jsonArray = jsonObject.get("jiesuanDetails").getAsJsonArray();
//            for (JsonElement element : jsonArray) {
//                JsonObject jsonObject1 = element.getAsJsonObject();
//                String accountedAtStr = jsonObject1.get("usetime").getAsString();
//                String outerOrderNo = jsonObject1.get("orderid58").getAsString();
//                BigDecimal businessAmount = jsonObject1.get("groupprice").getAsBigDecimal();
//                BigDecimal commissionFee = jsonObject1.get("commission").getAsBigDecimal();
//                BigDecimal settleAmount = jsonObject1.get("jiesuanmoney").getAsBigDecimal();
//
//            }
//        }
        return response;
    }

    public static String refundBill(String date) {
        //退款数据查询
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObject = new JsonObject();
        Map<String, Object> params = new HashMap<>();
        params.put("refundtime", date);
        String response = sendRequest1(params, "emc.groupbuy.queryrefundjiesuan", false, false, USE_GET_METHOD);
//        if (response.isOk()) {
//            jsonObject = response.data.getAsJsonObject();
//            jsonArray = jsonObject.get("refundDetails").getAsJsonArray();
//            for (JsonElement element : jsonArray) {
//                JsonObject jsonObject1 = element.getAsJsonObject();
//                String accountedAtStr = jsonObject1.get("refundtime").getAsString();
//                String outerOrderNo = jsonObject1.get("orderid58").getAsString();
//                BigDecimal businessAmount = jsonObject1.get("groupprice").getAsBigDecimal();
//                BigDecimal commissionFee = jsonObject1.get("commission").getAsBigDecimal();
//                BigDecimal settleAmount = jsonObject1.get("jiesuanmoney").getAsBigDecimal();
//            }
//        }
        return response;
    }


    public static JsonArray allProductTypes() {
        WubaResponse response = WubaUtil.sendRequest(null, "emc.groupbuy.find.allprotype", false, false, USE_POST_METHOD);
        return response.data.getAsJsonArray();
    }

    public static String allProductTypesJsonCache() {
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "ALL_PRODUCT_TYPES"),
                new CacheCallBack<String>() {
                    @Override
                    public String loadData() {
                        return allProductTypes().toString();
                    }
                });
    }


    /**
     * 默认请求需要加密，响应需要解密
     */
    public static WubaResponse sendRequest(Map<String, Object> appParams, String method) {
        return sendRequest(appParams, method, true, true, USE_POST_METHOD);
    }

    /**
     * 默认响应需要解密
     */
    public static WubaResponse sendRequest(Map<String, Object> appParams, String method, boolean requestNeedEncrypt) {
        return sendRequest(appParams, method, requestNeedEncrypt, true, USE_POST_METHOD);
    }

    /**
     * 调用58的接口
     *
     * @param appParams           post的参数列表
     * @param method              58的接口方法
     * @param requestNeedEncrypt  请求是否需要加密。对于部分接口不需加密的，传入false即可
     * @param responseNeedDecrypt 响应是否需要解密，对于部分接口不需解密的，传入false
     * @return 解析为json对象形式的58返回结果
     */
    public static WubaResponse sendRequest(Map<String, Object> appParams, String method,
                                           boolean requestNeedEncrypt, boolean responseNeedDecrypt,
                                           boolean postMethod) {
        // 系统级参数设置
        Map<String, Object> params = sysParams();
        params.put("m", method);

        String jsonRequest = new Gson().toJson(appParams);
        Logger.info("wuba request.%s:\n%s", method, jsonRequest);
        // 应用级参数设置
        if (requestNeedEncrypt) {
            params.put("param", encryptMessage(jsonRequest, SECRET_KEY));
        } else {
            if (postMethod) {
                params.put("param", jsonRequest);
            } else {
                for (Map.Entry<String, Object> entry : appParams.entrySet()) {
                    params.put(entry.getKey(), entry.getValue());
                }
            }
        }

        Logger.info("wuba request %s:\n%s", method, new Gson().toJson(params));

        WebServiceRequest paramRequest = WebServiceRequest.url(GATEWAY_URL)
                .type("58_" + method)
                .params(params).addKeyword("58");
        String json;
        if (postMethod) {
            json = paramRequest.postString();
        } else {
            json = paramRequest.getString();
        }
        Logger.info("wuba response:\n%s", json);

        WubaResponse result = parseResponse(json, responseNeedDecrypt);

        if (responseNeedDecrypt)
            Logger.info("wuba response decrypted: \n%s", result.toString());

        return result;
    }


    public static String sendRequest1(Map<String, Object> appParams, String method,
                                      boolean requestNeedEncrypt, boolean responseNeedDecrypt,
                                      boolean postMethod) {
        // 系统级参数设置
        Map<String, Object> params = sysParams();
        params.put("m", method);

        String jsonRequest = new Gson().toJson(appParams);
        Logger.info("wuba request.%s:\n%s", method, jsonRequest);
        // 应用级参数设置
        if (requestNeedEncrypt) {
            params.put("param", encryptMessage(jsonRequest, SECRET_KEY));
        } else {
            if (postMethod) {
                params.put("param", jsonRequest);
            } else {
                for (Map.Entry<String, Object> entry : appParams.entrySet()) {
                    params.put(entry.getKey(), entry.getValue());
                }
            }
        }

        Logger.info("wuba request %s:\n%s", method, new Gson().toJson(params));

        WebServiceRequest paramRequest = WebServiceRequest.url(GATEWAY_URL)
                .type("58_" + method)
                .params(params).addKeyword("58");
        String json;
        if (postMethod) {
            json = paramRequest.postString();
        } else {
            json = paramRequest.getString();
        }
        Logger.info("wuba response:\n%s", json);

        WubaResponse result = parseResponse(json, responseNeedDecrypt);

        if (responseNeedDecrypt)
            Logger.info("wuba response decrypted: \n%s", result.toString());

        return json;
    }

    /**
     * 解析58的返回信息
     *
     * @param jsonResponse json 文本
     * @return 解析后的对象
     */
    public static WubaResponse parseResponse(String jsonResponse, boolean needDecrypt) {
        JsonParser jsonParser = new JsonParser();

        JsonObject result = jsonParser.parse(jsonResponse).getAsJsonObject();

        WubaResponse response = new WubaResponse();

        if (result.has("status")) {
            response.status = result.get("status").getAsString();
        }
        response.code = result.get("code").getAsString();
        if (!result.get("msg").isJsonNull()) {
            response.msg = result.get("msg").getAsString();
        }
        if (result.has("data")) {
            String data = result.get("data").getAsString();
            if (response.isOk() && needDecrypt) {
                data = decryptMessage(data);
            }
            response.data = jsonParser.parse(data);
        }
        return response;
    }

    /**
     * 解析58的请求信息为json
     *
     * @param jsonRequest 58的请求信息
     * @return json对象
     */
    public static JsonObject parseRequest(String jsonRequest) {
        String decryptedMessage = decryptMessage(jsonRequest);
        JsonElement jsonElement = new JsonParser().parse(decryptedMessage);
        return jsonElement.getAsJsonObject();
    }


    public static String encryptMessage(String message) {
        return encryptMessage(message, SECRET_KEY);
    }

    /**
     * 加密提交给58的信息
     *
     * @param message 要加密的信息
     * @param key     密钥
     * @return 加密后的文本
     */
    public static String encryptMessage(String message, String key) {
        if (message == null) {
            throw new IllegalArgumentException("message to be encrypted can not be null");
        }
        if (key == null) {
            throw new RuntimeException("no wuba SECRET_KEY found");
        }

        try {
            //密钥只取前8位
            byte[] raw = key.getBytes(CODE_CHARSET);
            byte[] rawEffect = Arrays.copyOf(raw, 8);
            SecretKeySpec skeySpec = new SecretKeySpec(rawEffect, "DES");
            Cipher cipher = Cipher.getInstance(CODE_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

            //首先进行 urlencode，之后取得bytes
            String urlEncodedMessage = URLEncoder.encode(message, CODE_CHARSET);
            byte[] urlEncodedMessageBytes = urlEncodedMessage.getBytes(CODE_CHARSET);
            //然后进行加密
            byte[] desEncryptedBytes = cipher.doFinal(urlEncodedMessageBytes);
            //将加密结果转为16进制,之后取得bytes
            String hexString = Codec.byteToHexString(desEncryptedBytes);
            byte[] hexBytes = hexString.getBytes(CODE_CHARSET);
            //进行base64转码
            byte[] base64Bytes = Base64.encodeBase64(hexBytes);
            String base64String = new String(base64Bytes, CODE_CHARSET);
            //再次进行 urlencode
            return URLEncoder.encode(base64String, CODE_CHARSET);
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }

    public static String decryptMessage(String message) {
        return decryptMessage(message, SECRET_KEY);
    }

    /**
     * 解密58返回信息
     *
     * @param message 返回的信息
     * @param key     密钥
     * @return 解密后的信息
     */
    public static String decryptMessage(String message, String key) {
        if (message == null) {
            throw new IllegalArgumentException("message to be encrypted can not be null");
        }
        if (key == null) {
            throw new RuntimeException("no wuba SECRET_KEY found");
        }
        try {
            //密钥只取前8位
            byte[] raw = key.getBytes(CODE_CHARSET);
            byte[] rawEffect = Arrays.copyOf(raw, 8);
            SecretKeySpec skeySpec = new SecretKeySpec(rawEffect, "DES");
            Cipher cipher = Cipher.getInstance(CODE_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);

            //首先进行 urldecode，之后获取 bytes
            String urlDecodedStr = URLDecoder.decode(message, CODE_CHARSET);
            byte[] urlDecodedBytes = urlDecodedStr.getBytes(CODE_CHARSET);
            //做base64解码，之后生成字符串
            byte[] base64DecodedBytes = Base64.decodeBase64(urlDecodedBytes);
            String base64DecodedStr = new String(base64DecodedBytes, CODE_CHARSET);
            //将生成的16进制形式的字符串转为bytes
            byte[] hexBytes = Codec.hexStringToByte(base64DecodedStr);
            //进行解密
            byte[] decryptedBytes = cipher.doFinal(hexBytes);
            String decryptedStr = new String(decryptedBytes, CODE_CHARSET);
            //最后再做一次urldecode
            return URLDecoder.decode(decryptedStr, CODE_CHARSET);
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }

    private static Map<String, Object> sysParams() {
        Map<String, Object> paramMap = new HashMap<>();
        // 系统级参数设置（必须）
        paramMap.put("appid", WUBA_APP_KEY);
        paramMap.put("f", "json");
        paramMap.put("v", "1.0");
        paramMap.put("sn", "1");
        return paramMap;
    }
}
