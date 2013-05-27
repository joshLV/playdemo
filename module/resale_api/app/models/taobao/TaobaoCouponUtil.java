package models.taobao;

import com.google.gson.JsonObject;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.*;
import com.taobao.api.response.*;
import models.accounts.AccountType;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.order.*;
import models.resale.Resaler;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.exceptions.UnexpectedException;
import util.extension.ExtensionResult;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author likang
 *         Date: 12-11-27
 */
public class TaobaoCouponUtil {
    public static final String URL = Play.configuration.getProperty("taobao.top.url", "http://gw.api.taobao.com/router/rest");
    public static final String CODE_MERCHANT_APP_KEY = "21519243";
    public static final String CODE_MERCHANT_APP_SECRET_KEY = "cb95f0bb25cbd99917696314cdb6bc43";
    public static final long CODE_MERCHANT_ID = 1705483381L;
    public static final String CODE_MERCHANT_NICK = "kunranma";
    public static final String CODE_MERCHANT_POSID = "7517";

    /**
     * 告诉淘宝我券已经发过了.
     * 并设置outerOrder的status为恰当的值（但未保存，需要保存的话手动调用save()）
     *
     * @param outerOrder 淘宝的订单
     */
    public static void tellTaobaoCouponSend(OuterOrder outerOrder) {
        if (Play.runingInTestMode()) {
            return;
        }

        // 组合券
        List<ECoupon> eCoupons = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
        StringBuilder verifyCodes = new StringBuilder();
        for (int i = 0; i < eCoupons.size(); i++) {
            if (i != 0) {
                verifyCodes.append(",");
            }
            verifyCodes.append(eCoupons.get(i).getSafeECouponSN()).append(":1"); //1表示数量
        }

        JsonObject jsonObject = outerOrder.getMessageAsJsonObject();
        String token = jsonObject.get("token").getAsString();

        VmarketEticketSendRequest request = new VmarketEticketSendRequest();
        request.setOrderId(Long.parseLong(outerOrder.orderId));
        request.setVerifyCodes(verifyCodes.toString());
        request.setToken(token);

        TaobaoClient taobaoClient = new DefaultTaobaoClient(
                URL, outerOrder.resaler.taobaoCouponAppKey, outerOrder.resaler.taobaoCouponAppSecretKey);
        OAuthToken oAuthToken = getTokenOfTaobaoCodePlatform(outerOrder.resaler);
        if (outerOrder.resaler.taobaoCouponAppKey.equals(CODE_MERCHANT_APP_KEY)) {
            request.setCodemerchantId(CODE_MERCHANT_ID);
        }
        Logger.info("tell taobao coupon send request. orderId: %s, verifyCodes: %s, token: %s",
                request.getOrderId(), request.getVerifyCodes(), request.getToken());

        VmarketEticketSendResponse response;
        try {
            response = taobaoClient.execute(request, oAuthToken.accessToken);
        } catch (ApiException e) {
            Logger.info("tell taobao coupon send request exception. %s \n%s", outerOrder.id, e);
            return;//请求出错，忽略，等待下次重试
        }
        if (response == null) {
            Logger.info("tell taobao coupon send response. no response. %s", outerOrder.id);
            return;//请求出错，忽略，等待下次重试
        }

        if (response.getRetCode() != null && response.getRetCode() == 1) {
            outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
            Logger.info("tell taobao coupon send response. success. %s", outerOrder.id);
            return;//成功，设置为ORDER_SYNCED
        }

        Logger.info("tell taobao coupon send. outerOrderId:%s, sub_code:%s", outerOrder.id, response.getSubCode());

        if ("isv.eticket-send-error:code-alreay-send".equals(response.getSubCode())) {
            outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
            return;//如果错误是此订单已处理，那么也当做成功
        }

        if ("isv.eticket-service-unavailable:op-failed".equals(response.getSubCode())
                || "isv.eticket-service-unavailable:order-is-processing".equals(response.getSubCode())) {
            return;//淘宝操作失败，或者订单正在处理。这两种情况等会儿继续重试
        }

        if ("isv.eticket-order-status-error:invalid-order-status".equals(response.getSubCode())) {
            outerOrder.status = OuterOrderStatus.ORDER_IGNORE;
            return;//如果错误为淘宝订单状态异常，那么设置状态为忽略.
        }
        //剩余的错误都报警，将继续重试
        Logger.error("tell taobao coupon send. outerOrderId:%s, sub_code:%s", outerOrder.id, response.getSubCode());
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
        JsonObject jsonObject = outerOrder.getMessageAsJsonObject();
        String token = jsonObject.get("token").getAsString();

        // 组合券
        List<ECoupon> eCoupons = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
        StringBuilder verifyCodes = new StringBuilder();
        int i = 0;
        for (ECoupon coupon : eCoupons) {
            if (coupon.status != ECouponStatus.UNCONSUMED) {
                continue;
            }
            if (i != 0) {
                verifyCodes.append(",");
            }
            verifyCodes.append(coupon.getSafeECouponSN()).append(":1");
            i++;
        }

        VmarketEticketResendRequest request = new VmarketEticketResendRequest();
        request.setOrderId(Long.parseLong(outerOrder.orderId));
        request.setVerifyCodes(verifyCodes.toString());
        request.setToken(token);

        TaobaoClient taobaoClient = new DefaultTaobaoClient(
                URL, outerOrder.resaler.taobaoCouponAppKey, outerOrder.resaler.taobaoCouponAppSecretKey);
        OAuthToken oAuthToken = getTokenOfTaobaoCodePlatform(outerOrder.resaler);
        if (outerOrder.resaler.taobaoCouponAppKey.equals(CODE_MERCHANT_APP_KEY)) {
            request.setCodemerchantId(CODE_MERCHANT_ID);
        }

        Logger.info("tell taobao coupon resend request. orderId: %s, verifyCodes: %s, token: %s",
                request.getOrderId(), request.getVerifyCodes(), request.getToken());

        try {
            VmarketEticketResendResponse response = taobaoClient.execute(request, oAuthToken.accessToken);
            if (response != null) {
                Logger.info("tell taobao coupon resend response. ret code: %s", response.getRetCode());
//                return response.getRetCode() != null && response.getRetCode() == 1;
                return true;//暂时不管怎么样都表示这次重发结束了，如果淘宝那边报错了 那也没有办法 重试暂时没有意义.
            } else {
                Logger.info("tell taobao coupon resend response. no response");
            }
        } catch (ApiException e) {
            Logger.info("tell taobao coupon resend response raise exception. ", e);
        }
        return false;
    }

    /**
     * 在淘宝上验证.
     * 如果验证失败，则尝试撤销验证（可能是因为之前已经验证了），撤销成功的话重试。
     *
     * @param coupon 券
     * @return 是否验证通过
     */
    public static ExtensionResult verifyOnTaobao(ECoupon coupon) {
        if (coupon.partner != ECouponPartner.TB) {
            return ExtensionResult.INVALID_CALL;
        }

        OuterOrder outerOrder = OuterOrder.find("byPartnerAndYbqOrder", OuterOrderPartner.TB, coupon.order).first();
        if (outerOrder == null) {
            Logger.info("consume on taobao failed: outerOrder not found");
            return ExtensionResult.code(100).message("没有找到对应淘宝订单号（couponId:%d)", coupon.id);
        }
        JsonObject jsonObject = outerOrder.getMessageAsJsonObject();
        String token = jsonObject.get("token").getAsString();

        VmarketEticketConsumeRequest request = new VmarketEticketConsumeRequest();
        request.setOrderId(Long.parseLong(outerOrder.orderId));
        request.setVerifyCode(coupon.getSafeECouponSN());
        request.setConsumeNum(1L);
        request.setToken(token);

        TaobaoClient taobaoClient = new DefaultTaobaoClient(
                URL, outerOrder.resaler.taobaoCouponAppKey, outerOrder.resaler.taobaoCouponAppSecretKey);
        OAuthToken oAuthToken = getTokenOfTaobaoCodePlatform(outerOrder.resaler);
        if (outerOrder.resaler.taobaoCouponAppKey.equals(CODE_MERCHANT_APP_KEY)) {
            request.setCodemerchantId(CODE_MERCHANT_ID);
            request.setPosid(CODE_MERCHANT_POSID);
        }

        Logger.info("tell taobao coupon verify request. orderId: %s, verifyCode: %s, token: %s",
                request.getOrderId(), request.getVerifyCode(), request.getToken());

        try {
            VmarketEticketConsumeResponse response = taobaoClient.execute(request, oAuthToken.accessToken);
            if (response != null) {
                Logger.info("tell taobao coupon verify response. ret code: %s", response.getRetCode());

                if (response.getRetCode() != null && response.getRetCode() == 1L) {
                    Logger.info("verify success: coupon_sn: %s", coupon.eCouponSn);
                    coupon.partnerCouponId = response.getConsumeSecialNum();
                    coupon.save();
                    return ExtensionResult.SUCCESS;
                } else {
                    Logger.info("verify failed: coupon_sn: %s, try to reverseOnTaobao", coupon.eCouponSn);
                    //如果验证失败，首先尝试撤销验证，撤销成功的话继续验证。
                    if (reverseOnTaobao(coupon)) {
                        Logger.info("reverse coupon success! coupon_sn: %s", coupon.eCouponSn);
                        return verifyOnTaobao(coupon);
                    }
                }
            } else {
                Logger.info("tell taobao coupon verify response. no response");
                return ExtensionResult.code(101).message("调用淘宝接口无响应");
            }
        } catch (ApiException e) {
            Logger.info("tell taobao coupon verify response raise exception. ", e);
        }
        return ExtensionResult.code(102).message("调用淘宝接口出现异常");
    }

    /**
     * 调用淘宝的高性能版本的订单信息接口
     *
     * @param outerOrder 外部订单信息
     * @param fields     需要的参数
     * @return 淘宝订单信息
     */
    public static TradeGetResponse tradeInfo(OuterOrder outerOrder, String fields) {
        TradeGetRequest request = new TradeGetRequest();
        request.setFields(fields);
        request.setTid(Long.valueOf(outerOrder.orderId));

        TaobaoClient taobaoClient = new DefaultTaobaoClient(
                URL, outerOrder.resaler.taobaoCouponAppKey, outerOrder.resaler.taobaoCouponAppSecretKey);
        OAuthToken oAuthToken = OAuthToken.getOAuthToken(outerOrder.resaler.id, AccountType.RESALER, WebSite.TAOBAO);
        try {
            return taobaoClient.execute(request, oAuthToken.accessToken);
        } catch (ApiException e) {
            throw new RuntimeException("request taobao trade info error", e);
        }
    }

    /**
     * 在淘宝上撤销验证（冲正）
     */
    public static boolean reverseOnTaobao(ECoupon coupon) {
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndYbqOrder", OuterOrderPartner.TB, coupon.order).first();
        if (outerOrder == null) {
            Logger.info("consume on taobao failed: outerOrder not found");
            return false;
        }
        JsonObject jsonObject = outerOrder.getMessageAsJsonObject();
        String token = jsonObject.get("token").getAsString();

        VmarketEticketReverseRequest request = new VmarketEticketReverseRequest();
        request.setOrderId(Long.parseLong(outerOrder.orderId));
        request.setReverseCode(coupon.getSafeECouponSN());
        request.setReverseNum(1L);
        request.setConsumeSecialNum(coupon.partnerCouponId);
        request.setToken(token);

        TaobaoClient taobaoClient = new DefaultTaobaoClient(
                URL, outerOrder.resaler.taobaoCouponAppKey, outerOrder.resaler.taobaoCouponAppSecretKey);
        OAuthToken oAuthToken = getTokenOfTaobaoCodePlatform(outerOrder.resaler);
        if (outerOrder.resaler.taobaoCouponAppKey.equals(CODE_MERCHANT_APP_KEY)) {
            request.setCodemerchantId(CODE_MERCHANT_ID);
            request.setPosid(CODE_MERCHANT_POSID);
        }

        try {
            VmarketEticketReverseResponse response = taobaoClient.execute(request, oAuthToken.accessToken);
            if (response != null) {
                Logger.info("tell taobao coupon reverse response. ret code: %s", response.getRetCode());
                return response.getRetCode() != null && response.getRetCode() == 1L;
            }
        } catch (ApiException e) {
            Logger.info("tell taobao coupon reverse response raise exception. ", e);
        }

        return false;
    }


    /**
     * 获取淘宝电子凭证平台的OauthToken与一般使用方式不同。
     *
     * 在与淘宝电子凭证平台交互时，如果是自己发码，需要使用自身淘宝账户的token，如果是码商发码，需要使用码商的token
     *
     * @param resaler 分销商
     * @return 淘宝 OAuthToken
     */
    public static OAuthToken getTokenOfTaobaoCodePlatform(Resaler resaler) {
        if (resaler == null) {
            throw new RuntimeException("no resaler found");
        }
        OAuthToken token;
        if (resaler.taobaoCouponAppKey.equals(CODE_MERCHANT_APP_KEY)) {
            //如果是码商
            token = OAuthToken.find("byServiceUserIdAndWebSite", String.valueOf(CODE_MERCHANT_ID), WebSite.TAOBAO).first();
        }else {
            token =  OAuthToken.getOAuthToken(resaler.id, AccountType.RESALER, WebSite.TAOBAO);
        }
        if (token == null || token.isExpired()) {
            throw new UnexpectedException("!!!!!!!!!!!!!!!!!! 淘宝 token 过期 请联系技术人员(" + resaler.id + ")");
        }
        return token;
    }

    public static boolean verifyParam(String taobaoCouponServiceKey, Map<String, String> params) {
        String sign = params.get("sign");
        return sign != null && sign.equals(sign(taobaoCouponServiceKey, params));
    }

    public static String sign(String taobaoCouponServiceKey , Map<String, String> params) {
        StringBuilder paramString = new StringBuilder(taobaoCouponServiceKey);
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
