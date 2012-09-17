package models.dangdang;


import models.accounts.AccountType;
import models.order.ECoupon;
import models.order.Order;
import models.sales.Goods;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import play.Play;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 当当API工具类.
 * <p/>
 * User: sujie
 * Date: 9/13/12
 * Time: 2:02 PM
 */
public class DangDangApiUtil {

    private static final String MD5 = "MD5";
    private static final String VER = "1.0";
    private static final String XML = "XML";
    private static final String SIGN_METHOD = "1";

    private static final String SECRET_KEY = Play.configuration.getProperty("dangdang.secret_key", "x8765d9yj72wevshn");
    private static final String SPID = Play.configuration.getProperty("dangdang.spid", "3000003");


    private static final String SYNC_URL = "http://tuanapi.dangdang.com/team_inter_api/public/push_team_stock.php";
    private static final String QUERY_CONSUME_CODE_URL = "http://tuanapi.dangdang.com/team_open/query_consume_code.php";
    private static final String VERIFY_CONSUME_URL = "http://tuanapi.dangdang.com/team_open/verify_consume.php";

    /**
     * 返回一百券系统中商品总销量.
     * 调用当当的API
     *
     * @return
     */
    public static void syncSellCount(Goods goods) {
        int sellCount = goods.saleCount;

        String data = String.format("<data><row><spgid><![CDATA[%s]]></spgid><sellcount><![CDATA[%s]]></sellcount" +
                "></row></data>", goods.id, sellCount);
        try {
            Response response = DangDangApiUtil.access(SYNC_URL, data, "push_team_stock");
            //todo 返回结果处理
        } catch (DangDangException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * 查询当前券是否已在当当上退款了.
     *
     * @param eCoupon
     * @return
     */
    public static boolean isRefund(ECoupon eCoupon) throws DangDangException {
        DDOrderItem ddOrderOrderItem = DDOrderItem.findByOrder(eCoupon.orderItems);
        if (ddOrderOrderItem == null) {
            return false;
        }
        String data = String.format("<data><row><ddgid>%s</ddgid><type>%s</type><code>%s</code></row></data>",
                ddOrderOrderItem.ddgid, 1, eCoupon.eCouponSn);
        Response response = DangDangApiUtil.access(QUERY_CONSUME_CODE_URL, data, "query_consume_code");
        if (!response.success()) {
            throw new DangDangException(response.desc);
        }
        DDECouponStatus status = getStatus(response.data);
        return status.equals(DDECouponStatus.REFUNDED);
    }

    private static DDECouponStatus getStatus(Element data) {
        return data.elementText()

    }

    /**
     * 通知当当当前的券已经使用.
     *
     * @param eCoupon
     */
    public static void notifyVerified(ECoupon eCoupon) {
        DDOrderItem ddOrderOrderItem = DDOrderItem.findByOrder(eCoupon.orderItems);
        if (ddOrderOrderItem == null) {
            return;
        }
        String data = String.format("<data><row><ddgid>%s</ddgid><consume_code>%s</consume_code><verifycode>%s" +
                "</verifycode></row></data>",
                ddOrderOrderItem.ddgid, eCoupon.eCouponSn, eCoupon.eCouponSn);
        try {
            Response response = DangDangApiUtil.access(VERIFY_CONSUME_URL, data, "verify_consume");
            //todo  返回结果处理
        } catch (DangDangException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * 发送券号短信.
     *
     * @param data xml格式
     */
    public static Response sendSMS(String data) {

        Request<DDECoupon> request = new Request<>();

        // 订单摘要解析器
        Parser<DDECoupon> parser = new Parser<DDECoupon>() {
            @Override
            public DDECoupon parse(Element node) {
                DDECoupon eCoupon = new DDECoupon();
                eCoupon.orderId = Long.parseLong(node.elementTextTrim("order_id"));
                eCoupon.ddgid = Long.parseLong(node.elementTextTrim("ddgid"));
                eCoupon.spgid = Long.parseLong(node.elementTextTrim("spgid"));
                eCoupon.userCode = node.elementTextTrim("user_code");
                eCoupon.receiveMobile = node.elementTextTrim("receiveMobile");
                eCoupon.consumeId = node.elementTextTrim("consumeId");

                return eCoupon;
            }
        };
        Response response = new Response();

        response.ver = VER;
        response.spid = SPID;

        try {
            request.parseXml(data, "order", true, parser);
            List<DDECoupon> eCouponList = request.getNodeList();
            for (DDECoupon eCoupon : eCouponList) {
                Order ybqOrder = Order.find("dd_order_id=? and userId=? and userType=?", eCoupon.orderId, AccountType.RESALER, Long.parseLong(eCoupon.userCode)).first();
                if (ybqOrder == null) {
                    response.errorCode = ErrorCode.ORDER_NOT_EXITED;
                    response.desc = "没找到对应的订单";
                    break;
                }
                ECoupon coupon = ECoupon.find("order=? and eCouponSn=? and phone=?", ybqOrder, eCoupon.consumeId, eCoupon.receiveMobile).first();
                if (coupon == null) {
                    response.errorCode = ErrorCode.COUPON_SN_NOT_EXISTED;
                    response.desc = "没找到对应的券号";
                    break;
                }
                //最多发送三次短信，发送失败，则返回0
                if (!ECoupon.sendUserMessage(coupon.id)) {
                    response.errorCode = ErrorCode.MESSAGE_SEND_FAILED;
                    response.desc = "短信发送失败";
                    break;
                }

                //发送成功
                response.errorCode = ErrorCode.SUCCESS;
                response.desc = "success";
                response.addAttribute("consumeId", coupon.eCouponSn);
                response.addAttribute("ddOrderId", eCoupon.orderId);
                response.addAttribute("ybqOrderId", coupon.order.orderNumber);
            }

        } catch (DocumentException e) {
            response.errorCode = ErrorCode.PARSE_XML_FAILED;
            response.desc = "xml解析错误";
        }
        return response;

    }


    /**
     * 发送http请求，并返回xml
     *
     * @param url
     * @param data
     * @param apiName
     * @return
     */
    public static Response access(String url, String data, String apiName) throws DangDangException{
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
        postMethod.addParameter("data", data);
        String sign = getSign(data, time, apiName);
        postMethod.addParameter("sign", sign);
        try {
            //执行postMethod
            int statusCode = httpClient.executeMethod(postMethod); // HttpClient对于要求接受后继服务的请求，象POST和PUT等不能自动处理转发
            // 301或者302
            if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
                //从头中取出转向的地址
                return new Response(postMethod.getResponseBodyAsString());
            }
        } catch (Exception e) {
            throw new DangDangException(e.getMessage());
        }
        return null;
    }

    public static String getSign(String data, String time, String apiName) {
        byte[] result;
        String tt = " ";
        try {
            MessageDigest alg = java.security.MessageDigest.getInstance(MD5);
            alg.update((SPID + apiName + VER + data + SECRET_KEY + time).getBytes());
            result = alg.digest();
            for (int i = 0; i < result.length; i++) {
                tt += (char) result[i];
            }

            return tt;

        } catch (Exception ex) {
            return null;
        }

    }

    /**
     * 验证订单的签名sign
     *
     * @param params
     * @param appKey
     * @param appSecretKey
     * @param sign
     * @return
     */
    public static boolean validSign(SortedMap<String, String> params, String appKey, String appSecretKey, String sign) {
        StringBuilder signStr = new StringBuilder();
        for (SortedMap.Entry<String, String> entry : params.entrySet()) {
            signStr.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        signStr.append("app_secret_key=").append(appSecretKey);
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
