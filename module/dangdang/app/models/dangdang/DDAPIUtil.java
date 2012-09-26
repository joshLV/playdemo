package models.dangdang;


import models.accounts.AccountType;
import models.order.*;
import models.resale.Resaler;
import models.resale.ResalerStatus;
import models.sales.Goods;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.methods.PostMethod;
import org.dom4j.Element;
import play.Logger;
import play.Play;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 当当API工具类.
 * <p/>
 * User: sujie
 * Date: 9/13/12
 * Time: 2:02 PM
 */
public class DDAPIUtil {
    private static final String XML = "xml";
    private static final String SIGN_METHOD = "1";
    private static final String VER = Play.configuration.getProperty("dangdang.version", "1.0");
    private static final String SECRET_KEY = Play.configuration.getProperty("dangdang.secret_key", "x8765d9yj72wevshn");
    private static final String SPID = Play.configuration.getProperty("dangdang.spid", "3000003");
    private static final String SYNC_URL = Play.configuration.getProperty("dangdang.sync_url", "http://tuanapi.dangdang.com/team_open/public/push_team_stock.php");
    private static final String QUERY_CONSUME_CODE_URL = Play.configuration.getProperty("dangdang.query_consume_code_url", "http://tuanapi.dangdang.com/team_open/public/query_consume_code.php");
    private static final String VERIFY_CONSUME_URL = Play.configuration.getProperty("dangdang.verify_consume_url", "http://tuanapi.dangdang.com/team_open/public/verify_consume.php");
    public static final String DD_LOGIN_NAME = Play.configuration.getProperty("dangdang.resaler_login_name", "dangdang");

    public static HttpProxy proxy = new SimpleHttpProxy();

    /**
     * 返回一百券系统中商品总销量.
     * 调用当当的API
     *
     * @return
     */
    public static void syncSellCount(Goods goods) throws DDAPIInvokeException {
        String request = String.format("<data><row><spgid><![CDATA[%s]]></spgid><sellcount><![CDATA[%s]]></sellcount" +
                "></row></data>", goods.id, goods.saleCount);
        Response response = DDAPIUtil.access(SYNC_URL, request, "push_team_stock");
        if (!response.success()) {
            throw new DDAPIInvokeException("\ninvoke syncSellCount error(goodsId:" + goods.id + "):" +
                    "error_code:" + response.errorCode.getValue() + ",desc:" + response.desc);
        }
        Logger.info("[DangDang API] invoke syncSellCount success!");
    }

    /**
     * 查询当前券是否已在当当上退款了.
     * 调用当当的API
     *
     * @param eCoupon
     * @return
     */
    public static boolean isRefund(ECoupon eCoupon) throws DDAPIInvokeException {
        DDOrderItem ddOrderOrderItem = DDOrderItem.findByOrder(eCoupon.orderItems);
        if (ddOrderOrderItem == null) {
            Logger.info("[DangDang isRefund API] order item not found (eCouponSn:" + eCoupon.eCouponSn + ")!");
            return false;
        }
        String data = String.format("<data><row><ddgid><![CDATA[%s]]></ddgid><type><![CDATA[%s]]></type><code><![CDATA[%s]]></code></row></data>",
                ddOrderOrderItem.ddGoodsId, 1, eCoupon.eCouponSn);

        Response response = DDAPIUtil.access(QUERY_CONSUME_CODE_URL, data, "query_consume_code");

        if (!response.success()) {
            throw new DDAPIInvokeException("[DangDang API] invoke isRefund error(eCouponId:" + eCoupon.id + "):" + response.desc);
        }

        DDECouponStatus status = getStatus(response.data);
        return status.equals(DDECouponStatus.REFUNDED);
    }

    private static DDECouponStatus getStatus(Element data) {
        String state = data.elementText("state");
        return DDECouponStatus.getStatus(Integer.parseInt(state));
    }

    /**
     * 通知当当当前的券已经使用.
     * 调用当当的API
     *
     * @param eCoupon
     */
    public static void notifyVerified(ECoupon eCoupon) throws DDAPIInvokeException {
        DDOrderItem ddOrderOrderItem = DDOrderItem.findByOrder(eCoupon.orderItems);
        if (ddOrderOrderItem == null) {
            Logger.info("[DangDang notifyVerified API] order item not found (eCouponSn:" + eCoupon.eCouponSn + ")!");
            return;
        }
        String data = String.format("<data><row><ddgid><![CDATA[%s]]></ddgid><consume_code><![CDATA[%s]]></consume_code><verifycode><![CDATA[%s]]>" +
                "</verifycode></row></data>",
                ddOrderOrderItem.ddGoodsId, eCoupon.eCouponSn, eCoupon.eCouponSn);
        Response response = DDAPIUtil.access(VERIFY_CONSUME_URL, data, "verify_consume");

        if (!response.success()) {
            throw new DDAPIInvokeException("[DangDang API] invoke isRefund error(eCouponId:" + eCoupon.id + "):" + response.desc);
        }
    }

    /**
     * 发送券号短信.
     * 当当调用的API
     *
     * @param data xml格式
     */
    public static Response sendSMS(String data) throws DDAPIInvokeException {
        Logger.info("[DDSendMessageAPI] sendMsg begin]" + data);
        Response response = new Response();
        response.ver = VER;
        response.spid = SPID;
        try {
            Request request = new Request();
            request.parse(data);
            //取得data节点中的数据信息
            Map<String, String> dataMap = request.params;
            String orderId = dataMap.get("order_id");
            Long ddgid = Long.parseLong(dataMap.get("ddgid"));
            Long spgid = Long.parseLong(dataMap.get("spgid"));
            String userCode = dataMap.get("user_code");
            String receiveMobile = dataMap.get("receiver_mobile_tel");
            String consumeId = dataMap.get("consume_id");
            Logger.info("\n  orderId=" + orderId + "&ddgid=" + ddgid + "&spgid=" + spgid + "&userCode=" + userCode + "&=receiveMobile" + receiveMobile + "&=consumeId" + consumeId);

            //根据当当订单编号，查询订单是否存在
            OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderNumber",
                    OuterOrderPartner.DD, orderId).first();
            if (outerOrder == null || outerOrder.ybqOrder == null) {
                response.errorCode = ErrorCode.ORDER_NOT_EXITED;
                response.desc = "没找到对应的当当订单!";
                Logger.error("[DDSendMessageAPI]" + response.desc);
                return response;
            }
            Resaler resaler = Resaler.find("loginName=? and status=?", DD_LOGIN_NAME, ResalerStatus.APPROVED).first();

            if (resaler == null) {
                response.errorCode = ErrorCode.USER_NOT_EXITED;
                response.desc = "当当用户不存在！";
                Logger.error("[DDSendMessageAPI]" + response.desc);
                return response;
            }


            Order ybqOrder = Order.find("orderNumber= ? and userId=? and userType=?", outerOrder.ybqOrder.orderNumber, resaler.id, AccountType.RESALER).first();
            if (ybqOrder == null) {
                response.errorCode = ErrorCode.ORDER_NOT_EXITED;
                response.desc = "没找到对应的订单";
                Logger.error("[DDSendMessageAPI]" + response.desc);
                return response;
            }

            Goods goods = Goods.findById(spgid);
            ECoupon coupon = ECoupon.find("order=? and eCouponSn=? and goods=?", ybqOrder, consumeId, goods).first();
            if (coupon == null) {
                response.errorCode = ErrorCode.COUPON_SN_NOT_EXISTED;
                response.desc = "没找到对应的券号";
                Logger.error("[DDSendMessageAPI]" + response.desc);
                return response;
            }

            //最多发送三次短信，发送失败，则返回0
            if (!ECoupon.sendUserMessage(coupon.id, receiveMobile)) {
                response.errorCode = ErrorCode.MESSAGE_SEND_FAILED;
                response.desc = "短信发送失败(消费者只有三次发送短信的机会！)";
                Logger.error("[DDSendMessageAPI]" + response.desc);
                return response;
            }

            //发送成功
            response.errorCode = ErrorCode.SUCCESS;
            response.desc = "success";
            response.addAttribute("consumeId", coupon.eCouponSn);
            response.addAttribute("ddOrderId", orderId);
            response.addAttribute("ybqOrderId", coupon.order.id);
        } catch (Exception e) {
            throw new DDAPIInvokeException("[DangDang API] invoke send message error");
        }
        return response;

    }


    /**
     * 发送http请求，并返回xml
     * 调用当当的API
     *
     * @param url
     * @param request
     * @param apiName
     * @return
     */
    public static Response access(String url, String request, String apiName) throws DDAPIInvokeException {

        Logger.info("\nURL==============================" + url);
        Logger.info("\nRequest====" + request);

        //构造HttpClient的实例
        //创建GET方法的实例
        PostMethod postMethod = new PostMethod(url);
        //将表单的值放入postMethod中
        postMethod.addParameter("spid", SPID);
        postMethod.addParameter("result_format", XML);
        postMethod.addParameter("ver", VER);
        postMethod.addParameter("sign_method", SIGN_METHOD);
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        postMethod.addParameter("call_time", time);
        postMethod.addParameter("data", request);
        String sign = getSign(request, time, apiName);
        postMethod.addParameter("sign", sign);
        return proxy.accessHttp(postMethod);
    }

    public static String getSign(String data, String time, String apiName) {
        final String unsignedData = SPID + apiName + VER + data + SECRET_KEY + time;
        System.out.println("\nunsignedData   ====              [" + unsignedData + "]");
        final String signed = DigestUtils.md5Hex(unsignedData);
        System.out.println("\nsigned   ====            [" + signed + "]");
        return signed;
    }

    /**
     * 验证订单的签名sign
     *
     * @param params
     * @param sign
     * @return
     */
    public static boolean validSign(SortedMap<String, String> params, String sign) {
        StringBuilder signStr = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if ("body".equals(entry.getKey()) || "sign".equals(entry.getKey())) {
                continue;
            }
            System.out.println(entry.getKey() + "------------------" + entry.getValue());
            signStr.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue())).append("&");
        }

        signStr.append("sn=").append(SECRET_KEY);
        System.out.println(">>>>>>>>>." + DigestUtils.md5Hex(signStr.toString()));
        return DigestUtils.md5Hex(signStr.toString()).equals(sign);

    }

    /**
     * 获取参数信息
     *
     * @param params
     * @return
     */
    public static SortedMap<String, String> filterPlayParameter(Map<String, String[]> params) {
        SortedMap<String, String> result = new TreeMap<>();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            if ("body".equals(entry.getKey()) || "format".equals(entry.getKey())) {
                continue;
            }
            if (entry.getValue() == null) {
                result.put(entry.getKey(), "");
            } else {
                result.put(entry.getKey(), entry.getValue()[0]);
            }
        }
        return result;
    }
}
