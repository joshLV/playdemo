package models.api;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.SortedMap;

/**
 * @author likang
 *         Date: 12-7-18
 *         Time: 上午10:07
 */
public class Utils {
    public static boolean validSign(SortedMap<String,String> params, String appKey, String appSecretKey, String sign) {
        params.put("app_key", appKey);
        StringBuilder signStr = new StringBuilder();
        for(SortedMap.Entry<String,String> entry: params.entrySet()){
            signStr.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        signStr.append("app_secret_key=").append(appSecretKey);
        return DigestUtils.md5Hex(signStr.toString()).equals(sign);
    }
}
