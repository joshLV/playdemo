package models.dangdang;


import models.accounts.AccountType;
import models.order.ECoupon;
import models.order.Order;
import models.sales.Goods;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.dom4j.Element;
import play.Logger;
import play.Play;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

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
    private static final String VER = Play.configuration.getProperty("dangdang.version","1.0");
    private static final String SECRET_KEY = Play.configuration.getProperty("dangdang.secret_key", "x8765d9yj72wevshn");
    private static final String SPID = Play.configuration.getProperty("dangdang.spid", "3000003");
    private static final String SYNC_URL = Play.configuration.getProperty("dangdang.sync_url","http://tuanapi.dangdang.com/team_inter_api/public/push_team_stock.php");
    private static final String QUERY_CONSUME_CODE_URL = Play.configuration.getProperty("dangdang.sync_url","http://tuanapi.dangdang.com/team_open/query_consume_code.php");
    private static final String VERIFY_CONSUME_URL = Play.configuration.getProperty("dangdang.sync_url","http://tuanapi.dangdang.com/team_open/verify_consume.php");

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
            Logger.info("[DangDang isRefund API] order item not found (eCouponSn:"+eCoupon.eCouponSn+")!");
            return false;
        }
        String data = String.format("<data><row><ddgid><![CDATA[%s]]></ddgid><type><![CDATA[%s]]></type><code><![CDATA[%s]]></code></row></data>",
                ddOrderOrderItem.ddgid, 1, eCoupon.eCouponSn);

        Logger.info("QUERY_CONSUME_CODE_URL     ====="+QUERY_CONSUME_CODE_URL);
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
            Logger.info("[DangDang notifyVerified API] order item not found (eCouponSn:"+eCoupon.eCouponSn+")!");
            return;
        }
        String data = String.format("<data><row><ddgid><![CDATA[%s]]></ddgid><consume_code><![CDATA[%s]]></consume_code><verifycode><![CDATA[%s]]>" +
                "</verifycode></row></data>",
                ddOrderOrderItem.ddgid, eCoupon.eCouponSn, eCoupon.eCouponSn);
        Logger.info("VERIFY_CONSUME_URL     ====="+VERIFY_CONSUME_URL);
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
        Response response = new Response();
        response.ver = VER;
        response.spid = SPID;
        try {
            Request request = new Request();
            request.parse(data);
            //取得data节点中的数据信息
            Map<String, String> dataMap = request.getParams();
            Long orderId = Long.parseLong(dataMap.get("order_id"));
            Long ddgid = Long.parseLong(dataMap.get("ddgid"));
            Long spgid = Long.parseLong(dataMap.get("spgid"));
            String userCode = dataMap.get("user_code");
            String receiveMobile = dataMap.get("receiveMobile");
            String consumeId = dataMap.get("consumeId");

            //根据当当订单编号，查询订单是否存在
            DDOrder ddOrder = DDOrder.find("orderId=?", ddgid).first();
            if (ddOrder == null || ddOrder.ybqOrder == null) {
                response.errorCode = ErrorCode.ORDER_NOT_EXITED;
                response.desc = "没找到对应的订单";
                return response;
            }

            Order ybqOrder = Order.find("orderNumber= ? and userId=? and userType=?", ddOrder.ybqOrder.orderNumber, Long.parseLong(userCode), AccountType.RESALER).first();
            if (ybqOrder == null) {
                response.errorCode = ErrorCode.ORDER_NOT_EXITED;
                response.desc = "没找到对应的订单";
                return response;
            }
            Goods goods = Goods.findById(spgid);
            ECoupon coupon = ECoupon.find("order=? and eCouponSn=? and phone=? and goods=?", ybqOrder, consumeId, receiveMobile, goods).first();
            if (coupon == null) {
                response.errorCode = ErrorCode.COUPON_SN_NOT_EXISTED;
                response.desc = "没找到对应的券号";
                return response;
            }
            //最多发送三次短信，发送失败，则返回0
            if (!ECoupon.sendUserMessage(coupon.id)) {
                response.errorCode = ErrorCode.MESSAGE_SEND_FAILED;
                response.desc = "短信发送失败";
                return response;
            }

            //发送成功
            response.errorCode = ErrorCode.SUCCESS;
            response.desc = "success";
            response.addAttribute("consumeId", coupon.eCouponSn);
            response.addAttribute("ddOrderId", orderId);
            response.addAttribute("ybqOrderId", coupon.order.orderNumber);

        } catch (Exception e) {
            throw new DDAPIInvokeException(e.getMessage());
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
        Logger.info("\nRequest====" + request);

        //构造HttpClient的实例
        HttpClient httpClient = new HttpClient();
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
        Logger.info("\nsign   ====" + sign);
        postMethod.addParameter("sign", sign);
        try {
            //执行postMethod
            int statusCode = httpClient.executeMethod(postMethod); // HttpClient对于要求接受后继服务的请求，象POST和PUT等不能自动处理转发
            // 200
            if (statusCode == HttpStatus.SC_OK) {
                //从头中取出转向的地址
                return new Response(postMethod.getResponseBodyAsStream());
            }
        } catch (Exception e) {
            throw new DDAPIInvokeException(e.getMessage());
        }
        return new Response();
    }

    public static String getSign(String data, String time, String apiName) {

        final String unsignedData = SPID + apiName + VER + data + SECRET_KEY + time;
        System.out.println("\nunsignedData   ====              [" + unsignedData+"]");
        final String signed = DigestUtils.md5Hex(unsignedData);
        System.out.println("\nsigned   ====            [" + signed+"]");
        return signed;
    }

    /**
     * 验证订单的签名sign
     *
     * @param params
     * @param sign
     * @return
     */
    public static boolean validSign(Map<String, String> params, String sign) {
        StringBuilder signStr = new StringBuilder();
        for (SortedMap.Entry<String, String> entry : params.entrySet()) {
            if ("body".equals(entry.getKey()) || "sign".equals(entry.getKey())) {
                continue;
            }
            System.out.println(entry.getKey()+"------------------"+entry.getValue());
            signStr.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        signStr.append("secret_key=").append(SECRET_KEY);
        System.out.println(">>>>>>>>"+DigestUtils.md5Hex(signStr.toString()));
        return DigestUtils.md5Hex(signStr.toString()).equals(sign);
    }

    /**
     * 获取参数信息
     *
     * @param params
     * @return
     */
    public static Map<String, String> filterPlayParameter(Map<String, String[]> params) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            if ("body".equals(entry.getKey()) || "sign".equals(entry.getKey())) {
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
