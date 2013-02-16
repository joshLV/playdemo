package models.jingdong.groupbuy;

import cache.CacheCallBack;
import cache.CacheHelper;
import models.jingdong.groupbuy.response.IdNameResponse;
import models.jingdong.groupbuy.response.QueryIdNameResponse;
import models.jingdong.groupbuy.response.VerifyCouponResponse;
import models.order.ECoupon;
import models.order.OuterOrder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import play.Logger;
import play.Play;
import play.exceptions.UnexpectedException;
import play.libs.WS;
import play.libs.XML;
import play.libs.XPath;
import play.templates.Template;
import play.templates.TemplateLoader;
import util.ws.WebServiceRequest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-9-28
 */
public class JDGroupBuyUtil {
    public static final String VENDER_ID = Play.configuration.getProperty("jingdong.vender_id");
    public static final String VENDER_KEY = Play.configuration.getProperty("jingdong.vender_key");
    public static final String AES_KEY = Play.configuration.getProperty("jingdong.aes_key");

    public static final String CODE_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    public static final String CODE_CHARSET = "utf-8";

    public static String GATEWAY_URL = Play.configuration.getProperty("jingdong.gateway.url", "http://gw.tuan.360buy.net");

    public static final String CACHE_KEY = "JINGDGONG_API";

    /**
     * 解密REST信息
     *
     * @param message 解密前的信息
     * @return 解密后的信息
     */
    public static String decryptMessage(String message) {
        if (message == null) {
            throw new IllegalArgumentException("message to be decrypted can not be null");
        }
        if (AES_KEY == null) {
            throw new RuntimeException("no jingdong AES_KEY found");
        }

        try {
            // Base64解码
            byte[] base64Decoded = Base64.decodeBase64(message.getBytes(CODE_CHARSET));
            // AES解码
            byte[] raw = AES_KEY.getBytes(CODE_CHARSET);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance(CODE_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] aesEncodedBytes = cipher.doFinal(base64Decoded);

            return new String(aesEncodedBytes, CODE_CHARSET);
        } catch (Exception ex) {
            throw new UnexpectedException(ex);
        }
    }

    /**
     * 加密REST信息
     *
     * @param message 加密前的信息
     * @return 加密后的信息
     */
    public static String encryptMessage(String message) {
        if (message == null) {
            throw new IllegalArgumentException("message to be encrypted can not be null");
        }
        if (AES_KEY == null) {
            throw new RuntimeException("no jingdong AES_KEY found");
        }

        try {
            // AES编码
            byte[] raw = AES_KEY.getBytes(CODE_CHARSET);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance(CODE_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] messageBytes = message.getBytes(CODE_CHARSET);
            byte[] aesEncodedBytes = cipher.doFinal(messageBytes);
            // Base64编码
            return new String(Base64.encodeBase64(aesEncodedBytes), CODE_CHARSET);
        } catch (Exception ex) {
            throw new UnexpectedException(ex);
        }
    }

    /**
     * 请求京东的验证券
     *
     * @param eCoupon 一百券的券
     * @return 验证结果
     */
    public static boolean verifyOnJingdong(ECoupon eCoupon) {
        String url = GATEWAY_URL + "/platform/normal/verifyCode.action";

        //请求
        OuterOrder outerOrder = OuterOrder.find("byYbqOrder", eCoupon.order).first();
        if (outerOrder == null) {
            return false;
        }
        Template template = TemplateLoader.load("jingdong/groupbuy/request/verifyCoupon.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("outerOrder", outerOrder);
        params.put("coupon", eCoupon);
        String restRequestBody = makeRequestRest(template.render(params));

        Logger.info("jingdong request verifyCoupon:\n%s", restRequestBody);
        String responseResult = WebServiceRequest.url(url).type("jingdong_verify_order")
                .requestBody(restRequestBody)
                .addKeyword(outerOrder.orderId).addKeyword(eCoupon.id)
                .postString();

        //解析请求
        JDRest<VerifyCouponResponse> sendOrderJDRest = new JDRest<>();
        sendOrderJDRest.parse(responseResult, new VerifyCouponResponse());
        VerifyCouponResponse verifyCouponResponse = sendOrderJDRest.data;
        return verifyCouponResponse.verifyResult == 200;
    }

    /**
     * 查询城市
     *
     * @return 城市列表
     */
    public static List<IdNameResponse> queryCity() {
        String url = GATEWAY_URL + "/platform/normal/queryCityList.action";

        Template template = TemplateLoader.load("jingdong/groupbuy/request/queryCity.xml");
        String restRequest = makeRequestRest(template.render());
        Logger.info("jingdong request queryCity: %s", url);
        Logger.info("jingdong request queryCity:\n%s", restRequest);

        WS.HttpResponse response = WS.url(url).body(restRequest).post();

        JDRest<QueryIdNameResponse> queryCityRest = new JDRest<>();
        queryCityRest.parse(response.getString(), new QueryIdNameResponse("Cities"));
        return queryCityRest.data.idNameList;
    }

    public static List<IdNameResponse> cacheCities() {
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "CITIES"),
                new CacheCallBack<List<IdNameResponse>>() {
                    @Override
                    public List<IdNameResponse> loadData() {
                        return JDGroupBuyUtil.queryCity();
                    }
                });
    }

    /**
     * 查询一级分类
     *
     * @param categoryId 分类ID
     * @return 分类列表
     */
    public static List<IdNameResponse> queryCategory(Long categoryId) {
        String url = GATEWAY_URL + "/platform/normal/queryCategoryList.action";

        Template template = TemplateLoader.load("jingdong/groupbuy/request/queryCategory.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("categoryId", categoryId);
        String restRequest = makeRequestRest(template.render(params));
        Logger.info("jingdong request queryCategory:\n%s", restRequest);
        WS.HttpResponse response = WS.url(url).body(restRequest).post();

        JDRest<QueryIdNameResponse> queryCategoryRest = new JDRest<>();
        queryCategoryRest.parse(response.getString(), new QueryIdNameResponse("Categories"));

        return queryCategoryRest.data.idNameList;
    }

    public static List<IdNameResponse> cacheCategories(final Long categoryId) {
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "CATEGORIES_" + categoryId),
                new CacheCallBack<List<IdNameResponse>>() {
                    @Override
                    public List<IdNameResponse> loadData() {
                        return queryCategory(categoryId);
                    }
                }
        );
    }


    /**
     * 查询城市区域
     *
     * @param cityId 城市ID
     * @return 城市区域列表
     */
    public static List<IdNameResponse> queryDistrict(Long cityId) {
        String url = GATEWAY_URL + "/platform/normal/queryDistrictList.action";

        Template template = TemplateLoader.load("jingdong/groupbuy/request/queryDistrict.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("cityId", cityId);
        String restRequest = makeRequestRest(template.render(params));
        Logger.info("jingdong request queryDistrict:\n%s", restRequest);
        WS.HttpResponse response = WS.url(url).body(restRequest).post();

        JDRest<QueryIdNameResponse> queryDistrictRest = new JDRest<>();
        queryDistrictRest.parse(response.getString(), new QueryIdNameResponse("Districts"));
        return queryDistrictRest.data.idNameList;
    }

    public static List<IdNameResponse> cacheDistricts(final Long cityId) {
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "CITY_" + cityId + "_DISTRICTS"),
                new CacheCallBack<List<IdNameResponse>>() {
                    @Override
                    public List<IdNameResponse> loadData() {
                        return JDGroupBuyUtil.queryDistrict(cityId);
                    }
                }
        );
    }


    /**
     * 查询商圈
     *
     * @param districtId 区域ID
     * @return 商圈列表
     */
    public static List<IdNameResponse> queryArea(Long districtId) {
        String url = GATEWAY_URL + "/platform/normal/queryAreaList.action";

        Template template = TemplateLoader.load("jingdong/groupbuy/request/queryArea.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("districtId", districtId);
        String restRequest = makeRequestRest(template.render(params));
        Logger.info("jingdong request queryArea:\n%s", restRequest);
        WS.HttpResponse response = WS.url(url).body(restRequest).post();

        JDRest<QueryIdNameResponse> queryDistrictRest = new JDRest<>();
        queryDistrictRest.parse(response.getString(), new QueryIdNameResponse("Areas"));
        return queryDistrictRest.data.idNameList;
    }

    public static Map<Long, List<IdNameResponse>> cacheAreas(Long cityId) {
        final List<IdNameResponse> districts = cacheDistricts(cityId);
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "CITY_" + cityId + "_AREAS"),
                new CacheCallBack<Map<Long, List<IdNameResponse>>>() {
                    @Override
                    public Map<Long, List<IdNameResponse>> loadData() {
                        Map<Long, List<IdNameResponse>> areaMap = new HashMap<>();
                        for (IdNameResponse district : districts) {
                            List<IdNameResponse> areas = JDGroupBuyUtil.queryArea(district.id);
                            areaMap.put(district.id, areas);
                        }
                        return areaMap;
                    }
                }
        );
    }


    /**
     * 向京东发起请求.
     *
     * @param tag           请求标识，一般使用接口名称
     * @param url           接口URL
     * @param templatePath  使用的模板地址
     * @param params        模板参数
     * @param keywords      关键词
     * @return              解析后的京东响应消息
     */
    public static JingdongMessage sendRequest(String tag, String url, String templatePath, Map<String, Object> params,
                                              String ... keywords) {
        Template template = TemplateLoader.load(templatePath);
        String data = (params != null) ? template.render(params) : template.render();

        String restRequest = JDGroupBuyUtil.makeRequestRest(data);
        Logger.info("jingdong request %s:\n%s", tag, restRequest);

        WebServiceRequest request = WebServiceRequest.url(url).type("jingdong."+tag).requestBody(restRequest);
        for (String keyword : keywords) {
            request = request.addKeyword(keyword);
        }
        String response = request.postString();
        Logger.info("jingdong response %s:\n%s", tag, response);
        return parseMessage(response);
    }

    public static JingdongMessage parseMessage(String document) {
        return parseMessage(XML.getDocument(document));
    }

    /**
     * 解析京东的消息。包括我们请求京东的接口后京东的响应，以及京东主动通知我们的信息.
     *
     * @param document  xml形式的京东消息
     * @return          解析后的京东消息
     */
    public static JingdongMessage parseMessage(Document document) {
        JingdongMessage message = new JingdongMessage();
        message.version = XPath.selectText("/*/Version", document).trim();
        try{
            message.venderId = Long.parseLong(XPath.selectText("/*/VenderId", document).trim());
            message.zip = Boolean.parseBoolean(XPath.selectText("/*/Zip", document).trim());
            message.encrypt = Boolean.parseBoolean(XPath.selectText("/*/Encrypt", document).trim());
        }catch (Exception e) {
            return message;
        }

        // 只有作为京东的响应的时候， resultCode 和 resultMessage 才有用
        message.resultCode = XPath.selectText("/*/ResultCode", document);
        if (message.resultCode != null) message.resultCode = message.resultCode.trim();
        message.resultMessage = XPath.selectText("/*/ResultMessage", document);
        if (message.resultMessage != null) message.resultMessage = message.resultMessage.trim();

        if(message.encrypt){
            String rawMessage = XPath.selectText("/*/Data", document).trim();
            //解析加密字符串
            String decryptedMessage = JDGroupBuyUtil.decryptMessage(rawMessage);
            Logger.info("jingdong response decrypted:\n%s", decryptedMessage);

            message.message = XPath.selectNode("/Message", XML.getDocument(decryptedMessage));

        } else{
            message.message = XPath.selectNode("/*/Data/Message", document);
        }
        return message;
    }


    /**
     * 添加基本的请求参数，渲染完整REST请求内容
     *
     * @param data 核心请求内容
     * @return 完整的REST请求内容
     */
    public static String makeRequestRest(String data) {
        Template template = TemplateLoader.load("jingdong/groupbuy/request/main.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("version", "1.0");
        params.put("venderId", VENDER_ID);
        params.put("venderKey", VENDER_KEY);
        params.put("encrypt", "true");
        params.put("zip", false);
        params.put("data", encryptMessage(data));

        return template.render(params);
    }

}
