package models.wuba;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.order.ECoupon;
import org.apache.commons.codec.binary.Base64;
import play.Logger;
import play.Play;
import play.exceptions.UnexpectedException;
import play.libs.Codec;
import util.ws.WebServiceClient;
import util.ws.WebServiceClientFactory;

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
    public static final String GATEWAY_URL = Play.configuration.getProperty("wuba.gateway_url", "http://eapi.58.com:8080/api/rest");
//    public static final String GATEWAY_URL ="http://eapi.test.58v5.cn/api/rest";
    public static final String WUBA_APP_KEY = Play.configuration.getProperty("wuba.wuba_app_key");
    public static final String YBQ_APP_KEY = Play.configuration.getProperty("wuba.ybq_app_key");
    public static final String SECRET_KEY = Play.configuration.getProperty("wuba.secret_key");
    public static final String PARTNER_ID = Play.configuration.getProperty("wuba.partner_id");

    public static final String CODE_CHARSET = "utf-8";
    public static final String CODE_TRANSFORMATION = "DES/ECB/PKCS5Padding";

    public static boolean verifyOnWuba(ECoupon eCoupon) {
        Map<String, Object> params = new HashMap<>();
        params.put("ticketId", eCoupon.id);
        params.put("orderId", eCoupon.order.orderNumber);
        params.put("ticketIdIndex", 0);
        JsonObject result = sendRequest(params, "emc.groupbuy.order.ticketcheck");
        if ("10000".equals(result.get("status").getAsString())) {
            JsonObject data = result.get("data").getAsJsonObject();
            if (data.get("result").getAsInt() == 1) {
                return true;
            }
        }
        return false;
    }


    /**
     * 默认请求需要加密，响应需要解密
     */
    public static JsonObject sendRequest(Map<String, Object> appParams, String method) {
        return sendRequest(appParams, method, true, true);
    }

    /**
     * 默认响应需要解密
     */
    public static JsonObject sendRequest(Map<String, Object> appParams, String method, boolean requestNeedEncrypt) {
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
    public static JsonObject sendRequest(Map<String, Object> appParams, String method,
                                         boolean requestNeedEncrypt, boolean responseNeedDecrypt) {
        // 系统级参数设置
        Map<String, Object> params = sysParams();
        params.put("m", method);

        String jsonRequest = new Gson().toJson(appParams);
        // 应用级参数设置
        if (requestNeedEncrypt) {
            params.put("param", encryptMessage(jsonRequest, SECRET_KEY));
        } else {
            params.put("param", jsonRequest);
        }

        Logger.info("wuba request: \n%s", new Gson().toJson(params));

        WebServiceClient client = WebServiceClientFactory
                .getClientHelper();

        String json = client.postString("58_" + method,
                GATEWAY_URL, params, "58");

        Logger.info("wuba response: \n%s", json);

        JsonObject result = parseResponse(json, responseNeedDecrypt);

        Logger.info("wuba response decrypted: \n%s", result.toString());

        return result;
    }

    /**
     * 解析58的返回信息为json
     *
     * @param jsonResponse json 文本
     * @return json对象
     */
    public static JsonObject parseResponse(String jsonResponse, boolean decrypt) {
        try {
            JsonParser jsonParser = new JsonParser();

            JsonElement jsonElement = jsonParser.parse(jsonResponse);
            JsonObject result = jsonElement.getAsJsonObject();
            if (result.has("data")) {
                String data = result.get("data").getAsString();
                if (decrypt) {
                    data = decryptMessage(data);
                }
                JsonElement dataElement = jsonParser.parse(data);
                result.add("data", dataElement);
            }
            return result;
        } catch (Exception e) {
            Logger.error("Bad JSON: \n%s", jsonResponse);
            throw new RuntimeException("Cannot parse JSON (check logs)", e);
        }
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
