package models.wuba;

import cache.CacheCallBack;
import cache.CacheHelper;
import com.google.gson.*;
import models.order.ECoupon;
import models.order.ECouponPartner;
import org.apache.commons.codec.binary.Base64;
import play.Logger;
import play.Play;
import play.exceptions.UnexpectedException;
import play.libs.Codec;
import util.extension.ExtensionResult;
import util.ws.WebServiceRequest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
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

    public static JsonArray allProductTypes() {
        WubaResponse response = WubaUtil.sendRequest(null, "emc.groupbuy.find.allprotype", false, false);
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
        return sendRequest(appParams, method, true, true);
    }

    /**
     * 默认响应需要解密
     */
    public static WubaResponse sendRequest(Map<String, Object> appParams, String method, boolean requestNeedEncrypt) {
        return sendRequest(appParams, method, requestNeedEncrypt, true);
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
                                           boolean requestNeedEncrypt, boolean responseNeedDecrypt) {
        // 系统级参数设置
        Map<String, Object> params = sysParams();
        params.put("m", method);

        String jsonRequest = new Gson().toJson(appParams);
        Logger.info("wuba request.%s:\n%s",method, jsonRequest);
        // 应用级参数设置
        if (requestNeedEncrypt) {
            params.put("param", encryptMessage(jsonRequest, SECRET_KEY));
        } else {
            params.put("param", jsonRequest);
        }

        Logger.info("wuba request %s:\n%s", method, jsonRequest);
        String json = WebServiceRequest.url(GATEWAY_URL)
                .type("58_" + method)
                .params(params).addKeyword("58")
                .postString();
        Logger.info("wuba response:\n%s", json);

        WubaResponse result = parseResponse(json, responseNeedDecrypt);

        if (responseNeedDecrypt)
            Logger.info("wuba response decrypted: \n%s", result.toString());

        return result;
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

        if (result.has("status")){
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
