package models.baidu;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringEscapeUtils;
import play.Logger;
import play.Play;
import util.ws.WebServiceRequest;

import java.util.HashMap;
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
        params.put("data", jsonRequestData);
        Logger.info("baidu request %s:\n%s", method, new Gson().toJson(params));
        WebServiceRequest paramRequest = WebServiceRequest.url(GATEWAY_URL + method)
                .type("baidu_" + method).params(params).addKeyword("baidu");

        String json = paramRequest.postString();

        Logger.info("baidu response:\n%s", json);

        BaiduResponse result = parseResponse(json);

        Logger.info("baidu response decrypted: \n%s", result.toString());

        return result;
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

}
