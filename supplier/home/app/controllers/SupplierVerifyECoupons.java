package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uhuila.common.util.DateUtil;
import controllers.supplier.SupplierInjector;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.OrderItemsFeeType;
import models.order.SentAvailableECouponInfo;
import models.order.VerifyCouponType;
import models.resale.Resaler;
import models.sales.Shop;
import models.sales.SupplierResalerProduct;
import models.sales.SupplierResalerShop;
import models.sms.SMSMessage;
import models.supplier.Supplier;
import models.supplier.SupplierProperty;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.binding.As;
import play.libs.WS;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;
import util.transaction.RemoteRecallCheck;
import util.transaction.TransactionCallback;
import util.transaction.TransactionRetry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;

/**
 * 电子券验证.
 * <p/>
 * <p/>
 * User: sujie
 * Date: 12/19/12
 * Time: 9:59 AM
 */
@With({SupplierRbac.class, SupplierInjector.class})
@ActiveNavigation("coupons_multi_index")
public class SupplierVerifyECoupons extends Controller {
    private static String MEITUAN_LOGIN_NAME = "MEITUAN_LOGIN_NAME";
    private static String DIANPING_LOGIN_NAME = "DIANPING_LOGIN_NAME";
    private static String headers;

    @Before(priority = 1000)
    public static void storeShopIp() {
        SupplierUser supplierUser = SupplierRbac.currentUser();
        String strShopId = request.params.get("shopId");
        if (StringUtils.isNotBlank(strShopId)) {
            try {
                Long shopId = Long.parseLong(strShopId);
                if (supplierUser.lastShopId == null || !supplierUser.lastShopId.equals(shopId)) {
                    supplierUser.lastShopId = shopId;
                    supplierUser.save();
                }
            } catch (Exception e) {
                //ignore
            }
        }
        if (supplierUser.lastShopId != null) {
            renderArgs.put("shopId", supplierUser.lastShopId);
        }

    }

    /**
     * 券验证页面
     */
    public static void index() {
        Supplier supplier = SupplierRbac.currentUser().supplier;

        if ("1".equals(supplier.getProperty(Supplier.MEI_TUAN)) || "1".equals(supplier.getProperty(Supplier.DIAN_PING))) {
            redirect("/meituan-coupon/verified");
        }
        Long supplierUserId = SupplierRbac.currentUser().id;
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        List<Shop> shopList = Shop.findShopBySupplier(supplier.id);
        List<String> verifiedCoupons = ECoupon.getRecentVerified(supplierUser, 5);
        renderArgs.put("verifiedCoupons", verifiedCoupons);

        if (shopList.size() == 0) {
            error("该商户没有添加门店信息！");
        }

        if (supplierUser.shop == null) {
            render(shopList, supplierUser);
        } else {
            Shop shop = supplierUser.shop;
            //根据页面录入券号查询对应信息
            render(shop, supplierUser);
        }
    }

    /**
     * 查询
     *
     * @param eCouponSn 券号
     */
    @ActiveNavigation("coupons_verify")
    public static void singleQuery(Long shopId, String eCouponSn) {
        Long supplierId = SupplierRbac.currentUser().supplier.id;

        eCouponSn = StringUtils.trim(eCouponSn);

        if (StringUtils.isBlank(eCouponSn)) {
            renderJSON("{\"errorInfo\":\"券号不能为空！\"}");
        }
        Logger.info("SupplierVerifyECoupons.singleQuery: query eCouponSN (%s)", eCouponSn);
        //根据页面录入券号查询对应信息
        ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);

        //check券和门店
        String errorInfo = ECoupon.getECouponStatusDescription(ecoupon, shopId, "supplierVerify");
        if (StringUtils.isNotEmpty(errorInfo)) {
            Logger.info("SupplierVerifyECoupons.singleQuery: Error eCouponSN (%s), errorInfo: %s", eCouponSn, errorInfo);
            renderJSON("{\"errorInfo\":\"" + errorInfo + "\"}");
        } else {
            Logger.info("SupplierVerifyECoupons.singleQuery: success eCouponSN (%s) goods: %s", eCouponSn, ecoupon.goods.shortName);
            renderJSON("{\"goodsName\":\"" + ecoupon.goods.shortName + "\",\"faceValue\":" + ecoupon.faceValue
                    + ",\"expireAt\":\"" + DateUtil.dateToString(ecoupon.expireAt, 0) + "\"}");
        }
    }


    /**
     * 验证多个券
     */
    public static void verify(final Long shopId, String[] eCouponSns) {
        final Long supplierId = SupplierRbac.currentUser().supplier.id;
        final List<String> eCouponResult = new ArrayList<>();
        final List<ECoupon> needSmsECoupons = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(eCouponSns)) {
            for (String eCouponSn : eCouponSns) {
                final String stripedECouponSN = StringUtils.strip(eCouponSn);
                Logger.info("SupplierVerifyECoupons.verify: verify eCouponSN (%s)", stripedECouponSN);
                // 设置RemoteRecallCheck所使用的标识ID，下次调用时不会再重试.
                RemoteRecallCheck.setId("COUPON_" + eCouponSn);
                // 使用事务重试
                String result = TransactionRetry.run(new TransactionCallback<String>() {
                    @Override
                    public String doInTransaction() {
                        return doVerify(shopId, supplierId, stripedECouponSN, needSmsECoupons);
                    }
                });
                RemoteRecallCheck.cleanUp();
                Logger.info("SupplierVerifyECoupons.verify: verify eCouponSN (%s) result: %s", stripedECouponSN, result);
                eCouponResult.add(result != null ? result : "调用失败");
            }
        }
        sendVerifySMS(needSmsECoupons, shopId);
        renderJSON(eCouponResult);
    }

    private static String doVerify(Long shopId, Long supplierId,
                                   String eCouponSn, List<ECoupon> needSmsECoupons) {
        ECoupon ecoupon = ECoupon.query(eCouponSn, supplierId);
        String ecouponStatusDescription = ECoupon.getECouponStatusDescription(ecoupon, shopId, "supplierVerify");
        if (StringUtils.isNotEmpty(ecouponStatusDescription)) {
            return ecouponStatusDescription;
        }
        if (ecoupon.status == ECouponStatus.UNCONSUMED) {
            if (!ecoupon.consumeAndPayCommission(shopId, SupplierRbac.currentUser(), VerifyCouponType.SHOP)) {
                return "第三方" + ecoupon.partner + "券验证失败！请确认券状态(是否过期或退款等)！";
            }

            needSmsECoupons.add(ecoupon);
            return "消费成功.";
        }
        return "此券状态" + ecoupon.status + "非法！请联系客服";
    }

    /**
     * 获取最近验证过的n个券号.
     */
    public static void showVerifiedCoupons() {
        SupplierUser supplierUser = SupplierRbac.currentUser();
        List<String> verifiedCoupons = ECoupon.getRecentVerified(supplierUser, 5);
        renderJSON(verifiedCoupons);
    }

    @ActiveNavigation("coupons_verify")
    public static void refresh() {

    }

    private static void sendVerifySMS(List<ECoupon> eCoupons, Long shopId) {
        if (eCoupons.size() == 0) {
            return; //没有需要发的短信
        }
        final Shop shop = Shop.findById(shopId);
        Map<String, SentAvailableECouponInfo> map = new HashMap<>();
        SentAvailableECouponInfo ecouponInfo;
        for (ECoupon ae : eCoupons) {
            SentAvailableECouponInfo existedEcouponInfo = map.get(ae.orderItems.phone);
            if (existedEcouponInfo == null) {
                ecouponInfo = new SentAvailableECouponInfo();
                ecouponInfo.availableECouponSNs.add(ae.eCouponSn);
                ecouponInfo.sumFaceValue = ae.faceValue;
                ecouponInfo.lastECoupon = ae;
                map.put(ae.orderItems.phone, ecouponInfo);
            } else {
                existedEcouponInfo.availableECouponSNs.add(ae.eCouponSn);
                existedEcouponInfo.sumFaceValue = ae.faceValue;
                existedEcouponInfo.lastECoupon = ae;
            }
        }

        String dateTime = DateUtil.getNowTime();

        // 发给消费者
        for (String phone : map.keySet()) {
            new SMSMessage("您的券" + StringUtils.join(map.get(phone).availableECouponSNs, ",") + "(共" + map.get(phone).availableECouponSNs.size() + "张面值" + map.get(phone).sumFaceValue.setScale(2, BigDecimal.ROUND_HALF_UP) + "元)于" + dateTime
                    + "已成功消费，使用门店：" + shop.name + "。如有疑问请致电：4006865151", phone, map.get(phone).lastECoupon.replyCode)
                    .orderItemsId(map.get(phone).lastECoupon.orderItems.id)
                    .feeType(OrderItemsFeeType.SMS_VERIFY_NOTIFY)
                    .send2();
        }
    }


    public static void dianping() {

        Map<String, String> headers = getHeaders("dianping");
        headers.put("Content-Length", "33");

        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("userName", "172079");
        requestParams.put("password", "17248530");

        WS.WSRequest wsRequest = WS.url("http://e.dianping.com/account/login");
        wsRequest.headers(headers);
        wsRequest.body(requestParams);
        WS.HttpResponse wsResponse = wsRequest.post();
        System.out.println(wsResponse.getStatus() + "====");
        List<Http.Header> headList = wsResponse.getHeaders();
        for (Http.Header header : headList) {
            for (String s : header.values) {
                System.out.println(header.name + "-----" + s);
            }
        }


        String cookieValue;
        //美团 http://e.meituan.com/account/login

    }

    public static void meituan(String partnerGoodsId, String partnerShopId, @As(",") List<String> couponIds) {
        String message = "";
        if (StringUtils.isBlank(partnerGoodsId)) {
            message = "请选择美团项目！";
            renderJSON("{\"message\":\"" + message + "\"}");
        }
        if (StringUtils.isBlank(partnerShopId)) {
            message = "请选择门店！";
            renderJSON("{\"message\":\"" + message + "\"}");
        }
        if (couponIds == null) {
            message = "请输入券号！";
            renderJSON("{\"message\":\"" + message + "\"}");
        }
        Resaler resaler = Resaler.findApprovedByLoginName("meituan");
        Supplier supplier = SupplierRbac.currentUser().supplier;
        SupplierResalerShop supplierResalerShop = SupplierResalerShop.find("supplier=? and resaler=? and resalerPartnerShopId=?", supplier, resaler, partnerShopId).first();
        String cookie = "";
        if (supplierResalerShop != null) {
            cookie = supplierResalerShop.cookieValue;
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "*/*");
        headers.put("Accept-Encoding", "deflate,sdch");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8");
        headers.put("Cache-Control", "max-age=0");
        headers.put("Connection", "keep-alive");
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.116 Safari/537.36");

        headers.put("Host", "e.meituan.com");
        headers.put("Origin", "http://e.meituan.com");
        headers.put("Referer", "http://e.meituan.com/coupon/");
        headers.put("X-Requested-With", "XMLHttpRequest");

        headers.put("Cookie", cookie);
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < couponIds.size(); i++) {
            params.put("codes[" + i + "]", couponIds.get(i));
        }
        params.put("dealid", partnerGoodsId);
        params.put("bizloginid", partnerShopId);
        WS.HttpResponse response = WS.url("http://e.meituan.com/coupon/batchconsume").params(params).headers(headers).followRedirects(false).post();
        List<Http.Header> headerList = response.getHeaders();
        for (Http.Header header : headerList) {
            System.out.println(header.name + ": " + header.value());
        }
        String body = response.getString();
        System.out.println(body);
        JsonParser jsonParser = new JsonParser();
        JsonObject result = jsonParser.parse(body).getAsJsonObject();
        if (result.has("data")) {
            JsonArray dataResult = result.get("data").getAsJsonArray();
            for (JsonElement obj : dataResult) {
                JsonObject data = obj.getAsJsonObject();
//                String errcode = data.get("errcode").getAsString();//成功为false 失败为1
                message = data.get("result").getAsString();
            }
        }

        renderJSON("{\"message\":\"" + message + "\"}");
    }

    public static void meituan2() {
        //第一次登录
        Map<String, String> headers = getHeaders("meituan");

        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("login", "shysdwyh");
        requestParams.put("password", "270878");
        requestParams.put("remember_username", "1");
        requestParams.put("auto_login", "1");

        WS.WSRequest wsRequest = WS.url("http://e.meituan.com/account/login");
        wsRequest.followRedirects(true);
        wsRequest.headers(headers);
        wsRequest.params(requestParams);
        WS.HttpResponse wsResponse = wsRequest.post();

        List<Http.Header> headList = wsResponse.getHeaders();
        List<String> cookieList = new ArrayList<>();
        for (Http.Header header : headList) {
            if ("Set-Cookie".equals(header.name)) {
                for (String cookie : header.values) {
                    cookieList.add(cookie.split(";")[0]);
                }
            }
        }
        /*
        cookieList.add("em=Tjs");
        cookieList.add("om=Tjs");
        cookieList.add("xpiringdealnotitip=1");
        cookieList.add("shangfuchunjienotitip=1");
        cookieList.add("billschunjienotitip=1");
        cookieList.add("sellapplyconfirmtip=1");
        */
//        cookieList.add("euserguide=1");

        String cookie = StringUtils.join(cookieList, "; ");

        Map<String, String> headers1 = getHeaders("meituan");
        WS.WSRequest couponRequest = WS.url("http://e.meituan.com/coupon");
        headers1.put("Cookie", cookie);
//        headers.put("Cookie", "SID=6uicctq5ua52m4vo1d7vag45q7; eals=1; erus=1; elun=shysdwyh; ba=422331; elsu=shysdwyh; eal=Obiau7kogXZHVim6TMo_Zrx9vcsLWxbW; showdealconfirmnotitip=1; em=Tjs; om=Tjs; showexpiringdealnotitip=0; shangfuchunjienotitip=0; billschunjienotitip=0; sellapplyconfirmtip=0; __utma=1.489922958.1374483020.1374483020.1374483020.1; __utmb=1.1.10.1374483020; __utmc=1; __utmz=1.1374483020.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); uuid=b2ce3029490565b698b0.1374483019.0.0.0");
        headers1.put("Content-Length", "19");

        couponRequest.headers(headers1);
        for (Map.Entry<String, String> entry : headers1.entrySet()) {
            System.out.println("Header: " + entry.getKey() + ": " + entry.getValue());
        }

        Map<String, Object> couponRequestParams = new HashMap<>();
        couponRequestParams.put("code", "1111 1111 1111");
        WS.HttpResponse couponResponse = couponRequest.params(couponRequestParams).headers(headers).followRedirects(false).post();

        String body = couponResponse.getString();
        System.out.println("body: " + body);

        for (Http.Header header : couponResponse.getHeaders()) {
            System.out.println("header: " + header.name + ": " + header.value());
        }
    }

    public static Map<String, String> getHeaders(String partner) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("Accept-Encoding", "deflate,sdch");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8");
        headers.put("Cache-Control", "max-age=0");
        headers.put("Connection", "keep-alive");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.116 Safari/537.36");

        if (partner.equals("meituan")) {
            headers.put("Host", "e.meituan.com");
            headers.put("Origin", "http://e.meituan.com");
            headers.put("Referer", "http://e.meituan.com/coupon/");
        } else {

        }

        return headers;
    }

}
