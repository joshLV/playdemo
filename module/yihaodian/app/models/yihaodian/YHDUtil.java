package models.yihaodian;

import com.yhd.openapi.client.PostClient;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;

import java.security.MessageDigest;
import java.util.*;

/**
 * @author likang
 *         Date: 12-8-31
 */
public class YHDUtil {
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

    /**
     * 新的md5签名，首尾放secret。
     *
     * @param params 传给服务器的参数
     * @param secret 分配给您的APP_SECRET
     */
    public static String md5Signature(TreeMap<String, String> params, String secret) {
        String result = null;
        StringBuffer origin = getBeforeSign(params, new StringBuffer(secret));
        if (origin == null)
            return result;
        // secret last
        origin.append(secret);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            result = byte2hex(md.digest(origin.toString().getBytes("utf-8")));
        } catch (Exception e) {
            throw new java.lang.RuntimeException("sign error !");
        }
        return result;
    }
    /**
     * 二进制转字符串
     */
    private static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp = "";
        for (byte aB : b) {
            stmp = (Integer.toHexString(aB & 0XFF));
            if (stmp.length() == 1)
                hs.append("0").append(stmp);
            else
                hs.append(stmp);
        }
        return hs.toString();
    }
    /**
     * 添加参数的封装方法
     * @param params
     * @param orgin
     * @return
     */
    private static StringBuffer getBeforeSign(TreeMap<String, String> params, StringBuffer orgin) {
        if (params == null)
            return null;
        Map<String, String> treeMap = new TreeMap<>();
        treeMap.putAll(params);
        for (String name : treeMap.keySet()) {
            orgin.append(name).append(params.get(name));
        }
        return orgin;
    }

    public static TreeMap<String, String> filterPlayParams(Map<String, String> params){
        TreeMap<String, String> r = new TreeMap<>(params);
        r.remove("body");
        return r;
    }

    public static List<YHDErrorInfo> checkParam(TreeMap<String, String> params, String... keys){
        List<YHDErrorInfo> errorInfoList = new ArrayList<>();
        for (String key : keys){
            if(StringUtils.isBlank(params.get(key))){
                errorInfoList.add(new YHDErrorInfo("yhd.group.buy.order.inform.param_missing",
                        "参数 " + key +  " 不能为空", null));
            }
        }
        if(errorInfoList.size() > 0){
            return errorInfoList;
        }

        String sign = params.remove("sign");
        //检查参数签名
        String mySign = md5Signature(params, YHDUtil.SECRET_KEY);
        if(!mySign.equals(sign)){
            errorInfoList.add(new YHDErrorInfo("yhd.group.buy.order.inform.param_invalid", "sign不匹配", null));
        }
        params.put("sign", sign);//将sign重新塞回去以供保存
        //对一号店的参数进行trim
        for(Map.Entry<String, String> entry : params.entrySet()){
            params.put(entry.getKey(), entry.getValue().trim());
        }
        return errorInfoList;
    }
}
