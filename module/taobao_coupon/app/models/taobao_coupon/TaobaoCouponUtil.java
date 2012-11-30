package models.taobao_coupon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.VmarketEticketConsumeRequest;
import com.taobao.api.request.VmarketEticketResendRequest;
import com.taobao.api.request.VmarketEticketSendRequest;
import com.taobao.api.response.VmarketEticketConsumeResponse;
import com.taobao.api.response.VmarketEticketResendResponse;
import com.taobao.api.response.VmarketEticketSendResponse;
import models.accounts.AccountType;
import models.oauth.OAuthToken;
import models.order.ECoupon;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author likang
 *         Date: 12-11-27
 */
public class TaobaoCouponUtil {
    // 淘宝电子凭证的secret
    public static final String COUPON_SECRET = Play.configuration.getProperty("taobao.coupon.secret", "4f64e4c29f3790388965c9a095784d67");
    public static final String TOP_APPKEY = Play.configuration.getProperty("taobao.top.appkey", "21293912");
    public static final String TOP_APPSECRET = Play.configuration.getProperty("taobao.top.appsecret", "1781d22a1f06c4f25f1f679ae0633400");
    public static final String URL = Play.configuration.getProperty("taobao.top.url", "http://gw.api.taobao.com/router/rest");

    /**
     * 告诉淘宝我券已经发过了
     *
     * @param outerOrder 淘宝的订单
     * @return 是否告诉成功
     */
    public static boolean tellTaobaoCouponSend(OuterOrder outerOrder) {
        OAuthToken oAuthToken = getToken();
        if (oAuthToken == null) {
            oAuthToken = refreshToken();
        }

        // 组合券
        List<ECoupon> eCoupons = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
        StringBuilder verifyCodes = new StringBuilder();
        for (int i = 0 ;i < eCoupons.size(); i++) {
            if (i != 0) {
                verifyCodes.append(",");
            }
            verifyCodes.append(eCoupons.get(i).eCouponSn).append(":1");
        }

        TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, TOP_APPKEY, TOP_APPSECRET);
        JsonObject jsonObject = new JsonParser().parse(outerOrder.message).getAsJsonObject();
        String token = jsonObject.get("token").getAsString();

        VmarketEticketSendRequest request = new VmarketEticketSendRequest();
        request.setOrderId(outerOrder.orderId);
        request.setVerifyCodes(verifyCodes.toString());
        request.setToken(token);

        try {
            VmarketEticketSendResponse response = taobaoClient.execute(request, oAuthToken.accessToken);
            return response.getRetCode() == 1;
        } catch (ApiException e) {
            return false;
        }
    }

    /**
     * 告诉淘宝我要开始重新发券了,请他老人家心里有个谱
     *
     * @param outerOrder 淘宝的订单信息
     * @return 是否通知成功
     */
    public static boolean tellTaobaoCouponResend(OuterOrder outerOrder) {
        OAuthToken oAuthToken = getToken();
        if (oAuthToken == null) {
            oAuthToken = refreshToken();
        }
        JsonObject jsonObject = new JsonParser().parse(outerOrder.message).getAsJsonObject();
        String token = jsonObject.get("token").getAsString();

        // 组合券
        List<ECoupon> eCoupons = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
        StringBuilder verifyCodes = new StringBuilder();
        for (int i = 0 ;i < eCoupons.size(); i++) {
            if (i != 0) {
                verifyCodes.append(",");
            }
            verifyCodes.append(eCoupons.get(i).eCouponSn).append(":1");
        }

        TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, TOP_APPKEY, TOP_APPSECRET);
        VmarketEticketResendRequest request = new VmarketEticketResendRequest();
        request.setOrderId(outerOrder.orderId);
        request.setVerifyCodes(verifyCodes.toString());
        request.setToken(token);

        try {
            VmarketEticketResendResponse response = taobaoClient.execute(request, oAuthToken.accessToken);
            return response.getRetCode() == 1;
        } catch (ApiException e) {
            return false;
        }

    }

    /**
     * 在淘宝上验证
     *
     * @param eCoupon 券
     * @return 是否验证通过
     */
    public static boolean verifyOnTaobao(ECoupon eCoupon) {
        OAuthToken oAuthToken = getToken();
        if (oAuthToken == null) {
            oAuthToken = refreshToken();
        }
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndYbqOrder", OuterOrderPartner.TB, eCoupon.order).first();
        if (outerOrder == null) {
            Logger.info("consume on taobao failed: outerOrder not found");
            return false;
        }
        JsonObject jsonObject = new JsonParser().parse(outerOrder.message).getAsJsonObject();
        String token = jsonObject.get("token").getAsString();

        TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, TOP_APPKEY, TOP_APPSECRET);
        VmarketEticketConsumeRequest request = new VmarketEticketConsumeRequest();
        request.setOrderId(outerOrder.orderId);
        request.setVerifyCode(eCoupon.eCouponSn);
        request.setConsumeNum(1L);
        request.setToken(token);
        try {
            VmarketEticketConsumeResponse response = taobaoClient.execute(request,oAuthToken.accessToken);
            return response.getRetCode()  == 1L;
        } catch (ApiException e) {
            return false;
        }
    }

    public static OAuthToken getToken() {
        Resaler resaler = Resaler.findOneByLoginName(Resaler.TAOBAO_LOGIN_NAME);
        if (resaler == null) {
            throw new RuntimeException("no taobao resaler found");
        }
        return OAuthToken.find("byUserIdAndAccountType", resaler.id, AccountType.RESALER).first();
    }

    public static OAuthToken refreshToken() {
        OAuthToken token = getToken();
        if (token == null) {
        }
        return token;
    }

    public static boolean verifyParam(Map<String, String> params) {
        String sign = params.get("sign");
        return sign != null && sign.equals(sign(params));
    }

    public static String sign(Map<String, String> params) {
        params.remove("body");
        StringBuilder paramString = new StringBuilder(COUPON_SECRET);
        TreeMap<String, String> orderedParams = new TreeMap<>(params);
        for (String key : orderedParams.keySet()) {
            String value = orderedParams.get(key);
            if (!StringUtils.isBlank(value) && !"sign".equals(key)) {
                paramString.append(key).append(value);
            }
        }
        try {
            byte[] paramBytes = paramString.toString().getBytes("GBK");
            return new String(DigestUtils.md5(paramBytes), "GBK");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }


}
