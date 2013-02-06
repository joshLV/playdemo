package models.yihaodian;

import com.google.gson.Gson;
import com.yhd.openapi.client.PostClient;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import play.Logger;
import play.Play;
import play.libs.XML;
import play.libs.XPath;
import util.ws.WebServiceClient;
import util.ws.WebServiceRequest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
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

    public static YHDResponse sendRequest(Map<String,String> appParams, String method, String dataElementName){
        return sendRequest(appParams, null, method, dataElementName);
    }

    public static YHDResponse sendRequest(Map<String,String> appParams, File[] files, String method, String dataElementName){
        // 系统级参数设置
        Map<String, String> params = sysParams();
        params.put("method", method);
        // 应用级参数设置
        params.putAll(appParams);


        String sign = md5Signature(new TreeMap<>(params), SECRET_KEY);
        params.put("sign", sign);


        Map<String, Object> requestParams = new HashMap<>();
        for(Map.Entry<String, String> entry : params.entrySet()) {
            requestParams.put(entry.getKey(), entry.getValue());
        }

        Logger.info("yihaodian gateway_url: %s", GATEWAY_URL);
        Logger.info("yihaodian request %s:\n%s", method, new Gson().toJson(params));

        WebServiceRequest request = WebServiceRequest.url(GATEWAY_URL).type("yihaodian." + method).params(requestParams);
        if (files != null) {
            //todo
        }
        String documentStr = request.postString();
        Logger.info("yihaodian response:\n%s", documentStr);
        Document document = XML.getDocument(documentStr);

        return parseMessage(document, dataElementName);
    }

    public static YHDResponse parseMessage(Document document, String dataElementName) {
        YHDResponse response = new YHDResponse();
        response.errorCount = Integer.parseInt(XPath.selectText("/response/errorCount", document).trim());

        if (response.errorCount > 0) {
            response.errors = XPath.selectNodes("/response/errInfoList/errDetailInfo", document);
        }
        response.data = XPath.selectNode("/response/" + dataElementName , document);
        return response;
    }

    private static Map<String, String> sysParams(){
        Map<String, String> paramMap = new HashMap<>();
        // 系统级参数设置（必须）
        paramMap.put("checkCode", CHECK_CODE);
        paramMap.put("merchantId", MERCHANT_ID);
        paramMap.put("erp", ERP);
        paramMap.put("erpVer", ERP_VERSION);
        paramMap.put("format", FORMAT);
        paramMap.put("ver", VERSION);
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

        StringBuilder origin = new StringBuilder(secret);
        Map<String, String> treeMap = new TreeMap<>();
        treeMap.putAll(params);
        for (String name : treeMap.keySet()) {
            origin.append(name).append(params.get(name));
        }
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
