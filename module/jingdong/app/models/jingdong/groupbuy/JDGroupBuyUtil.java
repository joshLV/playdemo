package models.jingdong.groupbuy;

import models.jingdong.groupbuy.response.*;
import models.order.ECoupon;
import models.order.OuterOrder;
import org.apache.commons.codec.binary.Base64;
import play.Play;
import play.exceptions.UnexpectedException;
import play.libs.WS;
import play.templates.Template;
import play.templates.TemplateLoader;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-9-28
 */
public class JDGroupBuyUtil {
    public static final String VENDER_ID    = Play.configuration.getProperty("jingdong.vender_id");
    public static final String VENDER_KEY   = Play.configuration.getProperty("jingdong.vender_key");
    public static final String AES_KEY      = Play.configuration.getProperty("jingdong.aes_key");

    public static final String CODE_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    public static final String CODE_CHARSET = "utf-8";

    public static String GATEWAY_URL = Play.configuration.getProperty("jingdong.gateway.url", "http://gw.tuan.360buy.net");

    /**
     * 解密REST信息
     * @param message 解密前的信息
     * @return 解密后的信息
     */
    public static String decryptMessage(String message){
        if(message == null){
            throw new IllegalArgumentException("message to be decrypted can not be null");
        }
        if(AES_KEY == null){
            throw new RuntimeException("no jingdong AES_KEY found");
        }

        try {
            // Base64解码
            byte [] base64Decoded =  Base64.decodeBase64(message.getBytes(CODE_CHARSET));
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
    public static String encryptMessage(String message){
        if(message == null){
            throw new IllegalArgumentException("message to be encrypted can not be null");
        }
        if(AES_KEY == null){
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
            return new String(Base64.encodeBase64(aesEncodedBytes),CODE_CHARSET);
        } catch (Exception ex) {
            throw new UnexpectedException(ex);
        }
    }

    /**
     * 请求京东的验证券
     * @param eCoupon 一百券的券
     * @return 验证结果
     */
    public static boolean verifyOnJingdong(ECoupon eCoupon){
        String url = GATEWAY_URL + "/platform/normal/verifyCode.action";

        //请求
        OuterOrder outerOrder = OuterOrder.find("byYbqOrder", eCoupon.order).first();
        if(outerOrder == null){
            return false;
        }
        Template template = TemplateLoader.load("jingdong/groupbuy/request/verifyCoupon.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("outerOrder", outerOrder);
        params.put("coupon", eCoupon);
        String restRequest = makeRequestRest(template.render(params));
        WS.HttpResponse response =  WS.url(url).body(restRequest).post();

        //解析请求
        JDRest<VerifyCouponResponse> sendOrderJDRest = new JDRest<>();
        if(!sendOrderJDRest.parse(response.getString(), new VerifyCouponResponse())){
            return false;
        }
        VerifyCouponResponse verifyCouponResponse = sendOrderJDRest.data;
        return verifyCouponResponse.verifyResult == 200;
    }

    /**
     * 查询城市
     *
     * @return 城市列表
     */
    public static List<IdNameResponse> queryCity(){
        String url = GATEWAY_URL + "/platform/normal/queryCityList.action";

        Template template = TemplateLoader.load("jingdong/groupbuy/request/queryCity.xml");
        String restRequest = makeRequestRest(template.render());
        WS.HttpResponse response = WS.url(url).body(restRequest).post();

        JDRest<QueryIdNameResponse> queryCityRest = new JDRest<>();
        if(!queryCityRest.parse(response.getString(), new QueryIdNameResponse("Cities"))){
            return null;
        }
        return queryCityRest.data.idNameList;
    }

    /**
     * 查询一级分类
     * @param categoryId 分类ID
     * @return 分类列表
     */
    public static List<IdNameResponse> queryCategory(Long categoryId){
        String url = GATEWAY_URL + "/platform/normal/queryCategoryList.action";

        Template template = TemplateLoader.load("jingdong/groupbuy/request/queryCategory.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("categoryId", categoryId);
        String restRequest = makeRequestRest(template.render(params));
        WS.HttpResponse response = WS.url(url).body(restRequest).post();

        JDRest<QueryIdNameResponse> queryCategoryRest = new JDRest<>();
        if(!queryCategoryRest.parse(response.getString(), new QueryIdNameResponse("Categories"))){
            return null;
        }

        return queryCategoryRest.data.idNameList;
    }

    /**
     * 查询城市区域
     *
     * @param cityId 城市ID
     * @return 城市区域列表
     */
    public static List<IdNameResponse> queryDistrict(Long cityId){
        String url = GATEWAY_URL + "/platform/normal/queryDistrictList.action";

        Template template = TemplateLoader.load("jingdong/groupbuy/request/queryDistrict.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("cityId", cityId);
        String restRequest = makeRequestRest(template.render(params));
        WS.HttpResponse response = WS.url(url).body(restRequest).post();

        JDRest<QueryIdNameResponse> queryDistrictRest = new JDRest<>();
        if(!queryDistrictRest.parse(response.getString(), new QueryIdNameResponse("Districts"))){
            return null;
        }

        return queryDistrictRest.data.idNameList;
    }

    /**
     * 查询商圈
     * @param districtId 区域ID
     * @return 商圈列表
     */
    public static List<IdNameResponse> queryArea(Long districtId){
        String url = GATEWAY_URL + "/platform/normal/queryAreaList.action";

        Template template = TemplateLoader.load("jingdong/groupbuy/request/queryArea.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("districtId", districtId);
        String restRequest = makeRequestRest(template.render(params));
        WS.HttpResponse response = WS.url(url).body(restRequest).post();

        JDRest<QueryIdNameResponse> queryDistrictRest = new JDRest<>();
        if(!queryDistrictRest.parse(response.getString(), new QueryIdNameResponse("Areas"))){
            return null;
        }

        return queryDistrictRest.data.idNameList;
    }

    /**
     * 添加基本的请求参数，渲染完整REST请求内容
     * @param data 核心请求内容
     * @return 完整的REST请求内容
     */
    public static String makeRequestRest(String data){
        Template template = TemplateLoader.load("jingdong/groupbuy/request/main.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("version", "1.0");
        params.put("venderId", VENDER_ID);
        params.put("venderKey", VENDER_KEY);
        params.put("encrypt", "true");
        params.put("zip", false);
        params.put("data", data);

        return template.render(params);
    }

}
