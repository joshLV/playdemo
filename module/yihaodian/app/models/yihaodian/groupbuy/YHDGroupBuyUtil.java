package models.yihaodian.groupbuy;

import models.yihaodian.Util;
import models.yihaodian.groupbuy.response.YHDErrorInfo;
import models.yihaodian.groupbuy.response.YHDErrorResponse;
import org.jsoup.helper.StringUtil;

import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author likang
 *         Date: 12-9-13
 */
public class YHDGroupBuyUtil {
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

    public static TreeMap<String, String> filterPlayParams(Map<String, String[]> params){
        TreeMap<String, String> result = new TreeMap<>();

        for (Map.Entry<String, String[]> entry : params.entrySet()){
            if ("body".equals(entry.getKey())){
                continue;
            }
            if(entry.getValue() != null && entry.getValue().length > 0){
                result.put(entry.getKey(), entry.getValue()[0]);
            }else {
                result.put(entry.getKey(), "");
            }
        }
        return result;
    }

    public static YHDErrorResponse checkParam(TreeMap<String, String> params, String... keys){
        YHDErrorResponse errorResponse = new YHDErrorResponse();
        for (String key : keys){
            if(StringUtil.isBlank(params.get(key))){
                errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.order.inform.param_missing",
                        "参数 " + key +  " 不能为空", null));
            }
        }
        if(errorResponse.errorCount > 0){
            return errorResponse;
        }

        String sign = params.remove("sign");
        //检查参数签名
        String mySign = YHDGroupBuyUtil.md5Signature(params, Util.SECRET_KEY);
        if(!mySign.equals(sign)){
            errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.order.inform.param_invalid", "sign不匹配", null));
        }
        params.put("sign", sign);//将sign重新塞回去以供保存
        //对一号店的参数进行trim
        for(Map.Entry<String, String> entry : params.entrySet()){
            params.put(entry.getKey(), entry.getValue().trim());
        }
        return errorResponse;
    }
}
