package models.job.yihaodian;

import com.yhd.openapi.client.PostClient;

import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-8-31
 */
public class Util {
    public static String sendRequest(Map<String,String> appParams, String method){
        //测试环境URL
        String routerUrl = "http://211.144.198.140:7070/router/api/rest/router";
        //测试环境密钥
        String secretKey = "1234567890";

        // 系统级参数设置
        Map<String, String> params = sysParams();

        params.put("method", "yhd.general.products.search");
        // 应用级参数设置
        params.putAll(appParams);

        return PostClient.sendByPost(routerUrl, params, secretKey);
    }

    private static Map<String, String> sysParams(){
        Map<String, String> paramMap = new HashMap<>();
        // 系统级参数设置（必须）
        paramMap.put("checkCode", "11351-8414112-109759918-112-5555-123-20-21-93");
        paramMap.put("merchantId", "423");
        paramMap.put("erp", "cloudshops");
        paramMap.put("erpVer", "1.0");
        paramMap.put("format", "xml");
        paramMap.put("ver", "1.0");
        return paramMap;
    }
}
