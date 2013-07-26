package models.baidu;

import cache.CacheCallBack;
import cache.CacheHelper;
import com.google.gson.*;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.sales.ResalerProduct;
import models.sales.Shop;
import models.wuba.WubaResponse;
import org.apache.commons.lang.StringEscapeUtils;
import play.Logger;
import play.Play;
import util.extension.ExtensionResult;
import util.ws.WebServiceRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: yan
 * Date: 13-7-11
 * Time: 下午4:24
 */
public class BaiduUtil {
    public static final String GATEWAY_URL = Play.configuration.getProperty("baidu.gateway_url");
    public static final String BAIDU_USER_NAME = Play.configuration.getProperty("baidu.user_name");
    public static final String BAIDU_TOKEN = Play.configuration.getProperty("baidu.token");
    private static final String CACHE_KEY_CATEGORY = "BAIDU_CATEGORY_API";
    private static final String CACHE_KEY_CITY = "BAIDU_CITY_API";


    /**
     * 请求
     */
    public static BaiduResponse sendRequest(Map<String, Object> appParams, String method) {
        // 系统级参数设置
        Map<String, Object> param = sysParams();
        Map<String, Object> params = new HashMap<>();
        String jsonAuth = new Gson().toJson(param);
        params.put("auth", jsonAuth);
        String jsonRequestData = new Gson().toJson(appParams);

        Logger.info("wuba request.%s:\n%s", method, jsonRequestData);

        params.put("data", jsonRequestData);
        Logger.info("baidu request %s:\n%s", method, new Gson().toJson(params));
        WebServiceRequest paramRequest = WebServiceRequest.url(GATEWAY_URL + method)
                .type("baidu_" + method).params(params).encoding("UTF-8").addKeyword("baidu");

        String json = paramRequest.postString();

        Logger.info("baidu response:\n%s", json);

        BaiduResponse result = parseResponse(json);

        Logger.info("baidu response parser: \n%s", result.toString());

        return result;
    }

    /**
     * 在百度上验证.
     * 如果券是已经验证掉的，他们会直接返回成功.
     *
     * @param coupon 一百券自家券
     * @return 是否验证成功
     */
    public static ExtensionResult verifyOnBaidu(ECoupon coupon, Shop shop) {
        if (coupon.partner != ECouponPartner.BD) {
            return ExtensionResult.INVALID_CALL;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("tpid", "goodsLinkId");//goodsLinkId
        params.put("coupon", coupon.id.toString());
        params.put("poi_uid", shop.id.toString());
        BaiduResponse response = sendRequest(params, "verifytoken.action");
        if (response.isOk()) {
            return ExtensionResult.SUCCESS;
        }
        return ExtensionResult.code(1).message("百度券验证接口调用失败");
    }

    /**
     * 解析百度的返回信息
     *
     * @param jsonResponse json 文本
     * @return 解析后的对象
     */

    public static BaiduResponse parseResponse(String jsonResponse) {
        JsonParser jsonParser = new JsonParser();
        JsonObject result = jsonParser.parse(StringEscapeUtils.unescapeHtml(jsonResponse)).getAsJsonObject();

        BaiduResponse response = new BaiduResponse();
        response.code = result.get("code").getAsString();

        if (result.has("msg")) {
            response.msg = result.get("msg").getAsString();
        } else {
            if (!result.get("info").isJsonNull()) {
                response.msg = result.get("info").getAsString();
            }
        }
        if (response.isOk()) {
            if (result.has("res")) {
                response.res = result.get("res").getAsJsonObject();
                response.data = result.get("res").getAsJsonObject().get("data");
            }
        }

        return response;
    }

    /**
     * 应用系统参数
     */
    private static Map<String, Object> sysParams() {
        Map<String, Object> paramMap = new HashMap<>();
        // 系统级参数设置（必须）
        paramMap.put("token", BAIDU_TOKEN);
        paramMap.put("userName", BAIDU_USER_NAME);

        return paramMap;
    }
//[{"enName":"zhongcan","name":"中餐","parentId":29,"prodCategory":0,"prodTypeId":1},{"enName":"xican","name":"西餐","parentId":29,"prodCategory":0,"prodTypeId":55}]
    /**
     * 获取百度级分类
     */
    public static String allCategoryJsonCache() {
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY_CATEGORY, "BAIDU_CHILD_CATEGORY"),
                new CacheCallBack<String>() {
                    @Override
                    public String loadData() {
                        List<Map<String, Object>> mapList = new ArrayList<>();
                        BaiduResponse response = BaiduUtil.sendRequest(null, "getcategory.action");
                        for (JsonElement element : response.data.getAsJsonArray()) {
                            JsonObject obj = element.getAsJsonObject();
                            String parentId = obj.get("id").getAsString();
                            Map<String, Object> paramMap = new HashMap<>();
                            paramMap.put("id", parentId);
                            paramMap.put("level", "1");
                            BaiduResponse childResponse = BaiduUtil.sendRequest(paramMap, "getcategory.action");
                            //读取二级分类
                            for (JsonElement childElement : childResponse.data.getAsJsonArray()) {
                                JsonObject childObj = childElement.getAsJsonObject();
                                Map<String, Object> map = new HashMap<>();
                                map.put("prodTypeId", childObj.get("id").getAsString());
                                map.put("name", childObj.get("name").getAsString());
                                map.put("parentId", parentId);
                                map.put("prodCategory", "0");
                                mapList.add(map);
                            }
                        }
                        System.out.println(">>"+new Gson().toJson(mapList)+"=sss==");
                        return new Gson().toJson(mapList);
                    }
                });
    }


    public static String allCityJsonCache() {
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY_CITY, "ALL_BAIDU_CITIES"),
                new CacheCallBack<String>() {
                    @Override
                    public String loadData() {
                        Map<String, Object> paramMap = new HashMap<>();
                        // 系统级参数设置（必须）
                        paramMap.put("province_id", "5");
                        BaiduResponse response = BaiduUtil.sendRequest(paramMap, "getCity.action");
                        return response.data.getAsJsonArray().toString();
                    }
                });
    }
}
