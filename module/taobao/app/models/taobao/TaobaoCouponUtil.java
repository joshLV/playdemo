package models.taobao;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.VmarketEticketConsumeRequest;
import com.taobao.api.request.VmarketEticketResendRequest;
import com.taobao.api.request.VmarketEticketReverseRequest;
import com.taobao.api.request.VmarketEticketSendRequest;
import com.taobao.api.response.VmarketEticketConsumeResponse;
import com.taobao.api.response.VmarketEticketResendResponse;
import com.taobao.api.response.VmarketEticketReverseResponse;
import com.taobao.api.response.VmarketEticketSendResponse;
import models.accounts.AccountType;
import models.oauth.OAuthToken;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.exceptions.UnexpectedException;

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
        if (Play.runingInTestMode()) {
            return true;
        }
        OAuthToken oAuthToken = getToken();

        // 组合券
        List<ECoupon> eCoupons = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
        StringBuilder verifyCodes = new StringBuilder();
        for (int i = 0 ;i < eCoupons.size(); i++) {
            if (i != 0) {
                verifyCodes.append(",");
            }
            verifyCodes.append(eCoupons.get(i).getSafeECouponSN()).append(":1"); //1表示数量
        }

        TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, TOP_APPKEY, TOP_APPSECRET);
        JsonObject jsonObject = outerOrder.getMessageAsJsonObject();
        String token = jsonObject.get("token").getAsString();

        VmarketEticketSendRequest request = new VmarketEticketSendRequest();
        request.setOrderId(outerOrder.orderId);
        request.setVerifyCodes(verifyCodes.toString());
        request.setToken(token);
        Logger.info("tell taobao coupon send request. orderId: %s, verifyCodes: %s, token: %s",
                request.getOrderId(), request.getVerifyCodes(), request.getToken());

        try {
            VmarketEticketSendResponse response = taobaoClient.execute(request, oAuthToken.accessToken);
            if (response != null) {
                Logger.info("tell taobao coupon send response. ret code: %s", response.getRetCode());
                return response.getRetCode() != null && response.getRetCode() == 1;
            }else {
                Logger.info("tell taobao coupon send response. no response");
            }
        } catch (ApiException e) {
            Logger.info("tell taobao coupon send response raise exception. ", e);
        }
        return false;
    }

    /**
     * 告诉淘宝我要开始重新发券了,请他老人家心里有个谱
     *
     * @param outerOrder 淘宝的订单信息
     * @return 是否通知成功
     */
    public static boolean tellTaobaoCouponResend(OuterOrder outerOrder) {
        if (Play.runingInTestMode()) {
            return true;
        }
        OAuthToken oAuthToken = getToken();
        JsonObject jsonObject = outerOrder.getMessageAsJsonObject();
        String token = jsonObject.get("token").getAsString();

        // 组合券
        List<ECoupon> eCoupons = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
        StringBuilder verifyCodes = new StringBuilder();
        int i =0;
        for (ECoupon coupon : eCoupons) {
            if (coupon.status != ECouponStatus.UNCONSUMED) {
                continue;
            }
            if (i != 0) {
                verifyCodes.append(",");
            }
            verifyCodes.append(coupon.getSafeECouponSN()).append(":1");
            i ++;
        }

        TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, TOP_APPKEY, TOP_APPSECRET);
        VmarketEticketResendRequest request = new VmarketEticketResendRequest();
        request.setOrderId(outerOrder.orderId);
        request.setVerifyCodes(verifyCodes.toString());
        request.setToken(token);

        Logger.info("tell taobao coupon resend request. orderId: %s, verifyCodes: %s, token: %s",
                request.getOrderId(), request.getVerifyCodes(), request.getToken());

        try {
            VmarketEticketResendResponse response = taobaoClient.execute(request, oAuthToken.accessToken);
            if (response != null) {
                Logger.info("tell taobao coupon resend response. ret code: %s", response.getRetCode());
                return response.getRetCode() != null && response.getRetCode() == 1;
            }else {
                Logger.info("tell taobao coupon resend response. no response");
            }
        } catch (ApiException e) {
            Logger.info("tell taobao coupon resend response raise exception. ", e);
        }
        return false;
    }

    /**
     * 在淘宝上验证
     *
     * @param eCoupon 券
     * @return 是否验证通过
     */
    public static boolean verifyOnTaobao(ECoupon eCoupon) {
        OAuthToken oAuthToken = getToken();
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndYbqOrder", OuterOrderPartner.TB, eCoupon.order).first();
        if (outerOrder == null) {
            Logger.info("consume on taobao failed: outerOrder not found");
            return false;
        }
        JsonObject jsonObject = outerOrder.getMessageAsJsonObject();
        String token = jsonObject.get("token").getAsString();

        TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, TOP_APPKEY, TOP_APPSECRET);
        VmarketEticketConsumeRequest request = new VmarketEticketConsumeRequest();
        request.setOrderId(outerOrder.orderId);
        request.setVerifyCode(eCoupon.getSafeECouponSN());
        request.setConsumeNum(1L);
        request.setToken(token);

        Logger.info("tell taobao coupon verify request. orderId: %s, verifyCode: %s, token: %s",
                request.getOrderId(), request.getVerifyCode(), request.getToken());

        try {
            VmarketEticketConsumeResponse response = taobaoClient.execute(request,oAuthToken.accessToken);
            if (response != null){
                Logger.info("tell taobao coupon verify response. ret code: %s", response.getRetCode());

                if (response.getRetCode() != null && response.getRetCode() == 1L) {
                    eCoupon.partnerCouponId = response.getConsumeSecialNum();
                    eCoupon.save();
                    return true;
                }
            }else {
                Logger.info("tell taobao coupon verify response. no response");
            }
        } catch (ApiException e) {
            Logger.info("tell taobao coupon verify response raise exception. ", e);
        }
        return false;
    }

    /**
     * 在淘宝上撤销验证（冲正）
     */
    public static boolean reverseOnTaobao(ECoupon coupon) {
        OAuthToken oAuthToken = getToken();
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndYbqOrder", OuterOrderPartner.TB, coupon.order).first();
        if (outerOrder == null) {
            Logger.info("consume on taobao failed: outerOrder not found");
            return false;
        }
        JsonObject jsonObject = outerOrder.getMessageAsJsonObject();
        String token = jsonObject.get("token").getAsString();

        TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, TOP_APPKEY, TOP_APPSECRET);
        VmarketEticketReverseRequest request = new VmarketEticketReverseRequest();
        request.setOrderId(outerOrder.orderId);
        request.setReverseCode(coupon.getSafeECouponSN());
        request.setReverseNum(1L);
        request.setConsumeSecialNum(coupon.partnerCouponId);
        request.setToken(token);
        try {
            VmarketEticketReverseResponse response = taobaoClient.execute(request,oAuthToken.accessToken);
            if (response != null) {
                Logger.info("tell taobao coupon reverse response. ret code: %s", response.getRetCode());
                return response.getRetCode() != null && response.getRetCode() == 1L;
            }
        } catch (ApiException e) {
            Logger.info("tell taobao coupon reverse response raise exception. ", e);
        }

        return false;
    }



    public static OAuthToken getToken() {
        Resaler resaler = Resaler.findOneByLoginName(Resaler.TAOBAO_LOGIN_NAME);
        if (resaler == null) {
            throw new RuntimeException("no taobao resaler found");
        }
        OAuthToken token = OAuthToken.find("byUserIdAndAccountType", resaler.id, AccountType.RESALER).first();
        if (token == null || token.isExpired()) {
            /*
             2012-12-03
             淘宝的token 有效时间是一年，如果过期了，或者不小心删除了，
             请先清除oauth_token表中的token,然后使用taobao 这个账号登陆分销平台，
             然后随便找一个商品点击发布到淘宝，会跳转到淘宝的授权认证，点击授权后跳转回来之后，token就已经是最新的了
             */
            throw new UnexpectedException("!!!!!!!!!!!!!!!!!! 淘宝 token 过期 请联系技术人员, 或看此处代码的注释");
        }
        return token;
    }

    public static boolean verifyParam(Map<String, String> params) {
        String sign = params.get("sign");
        return sign != null && sign.equals(sign(params));
    }

    public static String sign(Map<String, String> params) {
        StringBuilder paramString = new StringBuilder(COUPON_SECRET);
        TreeMap<String, String> orderedParams = new TreeMap<>(params);
        for (String key : orderedParams.keySet()) {
            String value = orderedParams.get(key);
            if (!"sign".equals(key) && !"body".equals(key) && !StringUtils.isBlank(value)) {
                paramString.append(key).append(value);
            }
        }
        try {
            byte[] paramBytes = paramString.toString().getBytes("GBK");
            return DigestUtils.md5Hex(paramBytes).toUpperCase();
        } catch (UnsupportedEncodingException e) {
            Logger.warn("unsupported encoding gbk: %s", paramString.toString());
            return null;
        }
    }


}
