package models.sina;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.ECouponStatus;
import models.order.OuterOrderPartner;
import play.Logger;
import play.Play;
import play.libs.Codec;
import play.libs.WS;
import util.extension.ExtensionResult;
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
    public static String SOURCE_ID = Play.configuration.getProperty("sina.vouch.source_id");
    public static String SOURCE_NAME = Play.configuration.getProperty("sina.vouch.source_name");

    public final static String REQUEST_POST = "POST";
    public final static String REQUEST_PUT = "PUT";

    /**
     * 创建模板
     */
    public static SinaVoucherResponse uploadTemplate(String body) {
        return sendRequest("template", body, REQUEST_POST);
    }

    /**
     * 更新模板
     */
    public static SinaVoucherResponse updateTemplate(String body) {
        return sendRequest("template", body, REQUEST_PUT);
    }

    /**
     * 创建卡券
     */
    public static SinaVoucherResponse uploadVoucher(String body) {
        return sendRequest("vouch", body, REQUEST_POST);
    }

    /**
     * 请求新浪核销卡券
     */
    public static SinaVoucherResponse disposeVoucher(String body) {
        return sendRequest("vouch/dispose", body, REQUEST_PUT);
    }
    /**
     * 核销卡券
     */
    public static ExtensionResult disposeCoupon(ECoupon coupon) {
        if (coupon.partner != ECouponPartner.SINA) {
            Logger.info("not sina coupon partner:%s", coupon.partner);
            return ExtensionResult.INVALID_CALL;
        }
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("id", coupon.partnerCouponId);

        SinaVoucherResponse response = SinaVoucherUtil.disposeVoucher(new Gson().toJson(requestParams));
        if (!response.isOk()) {
            Logger.info("sina verify coupon fail,coupon:%s", coupon.eCouponSn);
            return ExtensionResult.INVALID_CALL;
        }
        return ExtensionResult.SUCCESS;
    }

    /**
     * 提交请求
     *
     * @param api api名称，与url中的标识相对应
     * @param body rest请求的body
     * @param requestType 请求类型 REQUEST_POST 或者是 REQUEST_PUT
     * @return 新浪的返回结果
     */
    public static SinaVoucherResponse sendRequest(String api, String body, String requestType) {

        //生成rest请求内容
        String restRequest = SinaVoucherUtil.makeRequestBody(body);
        Logger.info("sina voucher request %s:\n%s", api, restRequest);

        WebServiceRequest request = WebServiceRequest.url(GATEWAY + api).type("sina." + api).requestBody(restRequest);
        request.addHeader("Content-Type", "application/json;charset=utf-8");
        String result = "";
        if (REQUEST_POST.equals(requestType)) {
            result = request.postString();
        } else if (REQUEST_PUT.equals(requestType)) {
            result = request.putString();
        } else {
            throw new IllegalArgumentException("unknown request type: " + requestType);
        }

        Logger.info("sina voucher response %s:\n%s", api, result);
        return parseResponse(result);
    }

    /**
     * 解析处理响应
     *
     * @param jsonResponse 新浪返回的json字符串
     * @return 解析后的新浪返回结果
     */
    public static SinaVoucherResponse parseResponse(String jsonResponse) {
        JsonParser jsonParser = new JsonParser();
        JsonObject result = jsonParser.parse(jsonResponse).getAsJsonObject();

        SinaVoucherResponse response = new SinaVoucherResponse();
        if (result.has("error")) {
            response.error = result.getAsJsonObject("error");
        } else {
            response.header = result.getAsJsonObject("header");
            if (result.has("content")) {
                String content = result.get("content").getAsString();
                response.content = jsonParser.parse(content);
            }
        }

        return response;
    }

    /**
     * 组织请求信息
     *
     * @param content 请求的主体内容
     * @return 包装后的请求内容
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
