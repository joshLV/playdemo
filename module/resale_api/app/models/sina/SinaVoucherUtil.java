package models.sina;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import play.Logger;
import play.Play;
import play.libs.Codec;
import play.libs.WS;
import util.ws.WebServiceClient;
import util.ws.WebServiceRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * User: yan
 * Date: 13-3-21
 * Time: 上午10:00
 */
public class SinaVoucherUtil {
    public static String MEMBER_KEY = Play.configuration.getProperty("sina.vouch.member_key");
    public static String GATEWAY = Play.configuration.getProperty("sina.vouch.gateway.url");
    public static String SOURCE_ID=Play.configuration.getProperty("sina.vouch.source_id");
    public static String SOURCE_NAME=Play.configuration.getProperty("sina.vouch.source_name");

    public final static String REQUEST_POST = "POST";
    public final static String REQUEST_PUT = "PUT";

    /**
     * 创建模板
     *
     * @param body
     * @return
     */
    public static SinaVoucherResponse uploadTemplate(String body) {
        return sendRequest("template", body, REQUEST_POST);
    }

    /**
     * 提交请求
     *
     * @param api
     * @param body
     * @param requestType
     * @return
     */
    public static SinaVoucherResponse sendRequest(String api, String body, String requestType) {

        //生成rest请求内容
        String restRequest = SinaVoucherUtil.makeRequestBody(body);
        Logger.info("sina voucher request %s:\n%s", api, restRequest);

        WebServiceRequest request = WebServiceRequest.url(GATEWAY + api).type("sina." + api).requestBody(restRequest);
        String result = "";
        if (REQUEST_POST.equals(requestType)) {
            result = request.postString();
        } else if (REQUEST_PUT.equals(requestType)) {

        } else {
            throw new IllegalArgumentException("unknown request type: " + requestType);
        }

        Logger.info("sina voucher response %s:\n%s", api, result);
        return parseResponse(result);
    }

    /**
     * 解析处理响应
     *
     * @param jsonResponse
     * @return
     */
    public static SinaVoucherResponse parseResponse(String jsonResponse) {
        JsonParser jsonParser = new JsonParser();
        JsonObject result = jsonParser.parse(jsonResponse).getAsJsonObject();

        SinaVoucherResponse response = new SinaVoucherResponse();
        if (result.has("error")) {
            response.error = result.getAsJsonObject("error");
        }else {
            response.header = result.getAsJsonObject("header");
            String content = result.get("content").getAsString();
            response.content = jsonParser.parse(content);
        }

        return response;
    }

    /**
     * 组织请求信息
     *
     * @param content
     * @return
     */
    public static String makeRequestBody(String content) {
        Map<String, Object> params = new HashMap<>();
        Map<String, String> head = new HashMap<>();
        head.put("member_id", SOURCE_ID);
        head.put("sequence", UUID.randomUUID().toString());
        head.put("signature", sign(content, SOURCE_ID, MEMBER_KEY, head.get("sequence")));

        params.put("content", content);
        params.put("header", head);

        return new Gson().toJson(params);
    }

    /**
     * MD5加密
     */
    public static String sign(String content, String member_id, String key, String sequence) {
        return Codec.hexMD5(member_id + content + key + sequence);
    }
}
