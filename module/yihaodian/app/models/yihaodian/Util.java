package models.yihaodian;

import com.yhd.openapi.client.PostClient;
import play.Logger;
import play.Play;

import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-8-31
 */
public class Util {
    public static String GATEWAY_URL = Play.configuration.getProperty("yihaodian.gateway_url");
    public static String CHECK_CODE = Play.configuration.getProperty("yihaodian.check_code");
    public static String MERCHANT_ID = Play.configuration.getProperty("yihaodian.merchant_id");
    public static String SECRET_KEY = Play.configuration.getProperty("yihaodian.secret_key");

    public static String ERP = "self";
    public static String ERP_VERSION = "1.0";
    public static String FORMAT = "xml";
    public static String VERSION = "1.0";


    public static String sendRequest(Map<String,String> appParams, String method){
        // 系统级参数设置
        Map<String, String> params = sysParams();

        params.put("method", method);
        // 应用级参数设置
        params.putAll(appParams);

        Logger.info("gateway_url: %s", GATEWAY_URL );
        return PostClient.sendByPost(GATEWAY_URL, params, SECRET_KEY);
    }

    private static Map<String, String> sysParams(){
        Map<String, String> paramMap = new HashMap<>();
        // 系统级参数设置（必须）
        paramMap.put("checkCode", CHECK_CODE);
        paramMap.put("merchantId", MERCHANT_ID);
        paramMap.put("erp", "self");
        paramMap.put("erpVer", "1.0");
        paramMap.put("format", "xml");
        paramMap.put("ver", "1.0");
        return paramMap;
    }
}
