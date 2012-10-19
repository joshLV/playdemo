package models.jingdong.groupbuy;

import models.accounts.AccountType;
import models.jingdong.groupbuy.JDRest;
import models.jingdong.groupbuy.response.*;
import models.order.ECoupon;
import models.order.OuterOrder;
import models.resale.Resaler;
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

    public static String JD_LOGIN_NAME = Play.configuration.getProperty("jingdong.resaler_login_name", "jingdong");

    public static String GATEWAY_URL = Play.configuration.getProperty("jingdong.gateway.url", "http://gw.tuan.360buy.net");

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

    public static List<CityResponse> queryCity(){
        String url = GATEWAY_URL + "/platform/normal/queryCityList.action";

        Template template = TemplateLoader.load("jingdong/groupbuy/request/queryCity.xml");
        String restRequest = makeRequestRest(template.render());
        WS.HttpResponse response = WS.url(url).body(restRequest).post();

        JDRest<QueryCityResponse> queryCityRest = new JDRest<>();
        if(!queryCityRest.parse(response.getString(), new QueryCityResponse())){
            return null;
        }
        return queryCityRest.data.cities;
    }

    public static List<CategoryResponse> queryCategory(Long categoryId){
        String url = GATEWAY_URL + "/platform/normal/queryCategoryList.action";

        Template template = TemplateLoader.load("jingdong/groupbuy/request/queryCategory.xml");
        HashMap<String, Object> params = new HashMap<>();
        params.put("categoryId", categoryId);
        String restRequest = makeRequestRest(template.render(params));
        WS.HttpResponse response = WS.url(url).body(restRequest).post();

        JDRest<QueryCategoryResponse> queryCategoryRest = new JDRest<>();
        if(!queryCategoryRest.parse(response.getString(), new QueryCategoryResponse())){
            return null;
        }

        return queryCategoryRest.data.categories;
    }

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
