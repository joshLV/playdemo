package models.dangdang;


import com.uhuila.common.util.DateUtil;
import models.accounts.AccountType;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.resale.ResalerStatus;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.dom4j.Element;
import play.Logger;
import play.Play;
import play.libs.WS;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
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
    private static final String PUSH_PARTNER_TEAMS = Play.configuration.getProperty("dangdang.push_partner_teams", "http://tuanapi.dangdang.com/team_inter_api/public/push_partner_teams.php");
    private static final String GET_TEAM_LIST = Play.configuration.getProperty("dangdang.get_team_list", "http://tuanapi.dangdang.com/team_inter_api/public/get_team_list.php");


    public static HttpProxy proxy = new SimpleHttpProxy();

    /**
     * 返回一百券系统中商品总销量.
     * 调用当当的API
     *
     * @return
     */
    public static void syncSellCount(Goods goods) throws DDAPIInvokeException {
        //根据商品对应的GoodsDeployRelation的linkId
        Long id = goods.id;
        GoodsDeployRelation deployRelation = GoodsDeployRelation.getLast(id, OuterOrderPartner.DD);
        if (deployRelation != null) {
            id = deployRelation.linkId;
        }
        String request = String.format("<data><row><spgid><![CDATA[%s]]></spgid><sellcount><![CDATA[%s]]></sellcount" +
                "></row></data>", id, goods.getRealSaleCount());
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
        return !status.equals(DDECouponStatus.UNUSED);
    }

    private static DDECouponStatus getStatus(Element data) {
        if (data == null) {
            return null;
        }
        String state = data.elementText("state");
        return DDECouponStatus.getStatus(Integer.parseInt(state));
    }

    /**
     * 通知当当当前的券已经使用.
     * 调用当当的API
     *
     * @param eCoupon
     */
    public static void notifyVerified(ECoupon eCoupon) {
        DDOrderItem ddOrderOrderItem = DDOrderItem.findByOrder(eCoupon.orderItems);
        if (ddOrderOrderItem == null) {
            Logger.info("[DangDang notifyVerified API] order item not found (eCouponSn:" + eCoupon.eCouponSn + ")!");
            return;
        }
        String data = String.format("<data><row><ddgid><![CDATA[%s]]></ddgid><consume_code><![CDATA[%s]]></consume_code><verifycode><![CDATA[%s]]>" +
                "</verifycode></row></data>",
                ddOrderOrderItem.ddGoodsId, eCoupon.eCouponSn, eCoupon.eCouponSn);
        Response response = null;
        try {
            response = DDAPIUtil.access(VERIFY_CONSUME_URL, data, "verify_consume");
        } catch (DDAPIInvokeException e) {
            Logger.info("[DangDang API] invoke isRefund error(eCouponId:" + eCoupon.id + "):" + response.desc);
            logFailure(new DDFailureLog(eCoupon, response));
            return;
        }

        if (!response.success()) {
            Logger.info("[DangDang API] invoke isRefund error(eCouponId:" + eCoupon.id + "):" + response.desc);
            logFailure(new DDFailureLog(eCoupon, response));
        }
    }

    /**
     * 记录当当接口调用失败日志.
     */
    private static void logFailure(DDFailureLog log) {
//        Logger.info("!!!!!!!log Failure:log.eCouponId:" + log.eCouponId);
        log.save();
    }

    /**
     * 发送券号短信.
     * 当当调用的API
     *
     * @param data xml格式
     */
    public static Response sendSMS(String data) {
        Logger.info("[DDAPIUtil.sendSMS] sendMsg begin]" + data);
        Response response = new Response();
        Request request = new Request();
        response.ver = VER;
        response.spid = SPID;
        try {
            request.parse(data);
        } catch (Exception e) {
            response = new Response();
            response.spid = SPID;
            response.ver = VER;
            response.errorCode = ErrorCode.PARSE_XML_FAILED;
            response.desc = "xml解析失败！";
            return response;
        }
        //取得data节点中的数据信息
        Map<String, String> dataMap = request.params;
        String orderId = dataMap.get("order_id");
        Long spgid = Long.parseLong(dataMap.get("spgid"));
        String receiveMobile = dataMap.get("receiver_mobile_tel");
        String consumeId = dataMap.get("consume_id");

        //根据当当订单编号，查询订单是否存在
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId",
                OuterOrderPartner.DD, Long.valueOf(orderId)).first();
        if (outerOrder == null || outerOrder.ybqOrder == null) {
            response.errorCode = ErrorCode.ORDER_NOT_EXITED;
            response.desc = "没找到对应的当当订单!";
            Logger.info("[DDAPIUtil.sendSMS]" + response.desc);
            return response;
        }
        Resaler resaler = Resaler.find("loginName=? and status=?", Resaler.DD_LOGIN_NAME, ResalerStatus.APPROVED).first();

        if (resaler == null) {
            response.errorCode = ErrorCode.USER_NOT_EXITED;
            response.desc = "当当用户不存在！";
            Logger.info("[DDAPIUtil.sendSMS]" + response.desc);
            return response;
        }


        Order ybqOrder = Order.find("orderNumber= ? and userId=? and userType=?", outerOrder.ybqOrder.orderNumber, resaler.id, AccountType.RESALER).first();
        if (ybqOrder == null) {
            response.errorCode = ErrorCode.ORDER_NOT_EXITED;
            response.desc = "没找到对应的订单!";
            Logger.info("[DDAPIUtil.sendSMS]" + response.desc);
            return response;
        }

        //从对应商品关系表中取得商品
        Goods goods = GoodsDeployRelation.getGoods(OuterOrderPartner.DD, spgid);
        if (goods == null) {
            goods = Goods.findById(spgid);
        }
        ECoupon coupon = ECoupon.find("order=? and eCouponSn=? and goods=?", ybqOrder, consumeId, goods).first();
        if (coupon == null) {
            response.errorCode = ErrorCode.COUPON_SN_NOT_EXISTED;
            response.desc = "没找到对应的券号!";
            Logger.info("[DDAPIUtil.sendSMS]" + response.desc);
            return response;
        }
        //券已消费
        if (coupon.status == ECouponStatus.CONSUMED) {
            response.errorCode = ErrorCode.COUPON_CONSUMED;
            response.desc = "对不起该券已消费，不能重发短信！";
            Logger.info("[DDAPIUtil.sendSMS]" + response.desc);
            return response;
        }
        //券已退款
        if (coupon.status == ECouponStatus.REFUND) {
            response.errorCode = ErrorCode.COUPON_REFUND;
            response.desc = "对不起该券已退款，不能重发短信！";
            Logger.info("[DDAPIUtil.sendSMS]" + response.desc);
            return response;
        }
        //券已过期
        if (coupon.expireAt.before(new Date())) {
            response.errorCode = ErrorCode.COUPON_EXPIRED;
            response.desc = "对不起该券已过期，不能重发短信！";
            Logger.info("[DDAPIUtil.sendSMS]" + response.desc);
            return response;
        }
        //最多发送三次短信
        if (coupon.downloadTimes == 0) {
            response.errorCode = ErrorCode.MESSAGE_SEND_FAILED;
            response.desc = "重发短信超过三次！";
            Logger.info("[DDAPIUtil.sendSMS]" + response.desc);
            return response;
        }

        //发送短信并返回成功
        ECoupon.sendUserMessageWithoutCheck(receiveMobile, coupon);
        response.errorCode = ErrorCode.SUCCESS;
        response.desc = "success";
        response.addAttribute("consumeId", coupon.eCouponSn);
        response.addAttribute("ddOrderId", orderId);
        response.addAttribute("ybqOrderId", coupon.order.id);

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
        Logger.info("\n request URL=================" + url);

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
        postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
        return proxy.accessHttp(postMethod);
    }

    public static String getSign(String data, String time, String apiName) {
        final String unsignedData = SPID + apiName + VER + data + SECRET_KEY + time;
        final String signed = DigestUtils.md5Hex(unsignedData);
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
            signStr.append(entry.getKey()).append("=").append(WS.encode(entry.getValue())).append("&");
        }

        signStr.append("sn=").append(SECRET_KEY);
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

    /**
     * 发布商品
     *
     * @param linkId       商品对应的ID
     * @param requestItems
     * @throws DDAPIInvokeException
     */
    public static boolean pushGoods(Long linkId, String requestItems) throws DDAPIInvokeException {
        Response response = DDAPIUtil.access(PUSH_PARTNER_TEAMS, requestItems, "push_partner_teams");
        if (!response.success()) {
            throw new DDAPIInvokeException("\n invoke push goods error(linkId:" + linkId + "):" +
                    "error_code:" + response.errorCode.getValue() + ",desc:" + response.desc);
        }
        return response.errorCode != ErrorCode.SUCCESS;
    }


    /**
     * 当当项目查询接口
     *
     * @param linkId
     * @return
     * @throws DDAPIInvokeException
     */
    public static String getItemList(Long linkId) throws DDAPIInvokeException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String requestItems = String.format("<data><start_date><![CDATA[%s]]></start_date>" +
                "<end_date><![CDATA[%s]]></end_date><status><![CDATA[10]]></status></data>", dateFormat.format(DateUtil.getBeginOfDay()),
                dateFormat.format(DateUtil.getEndOfDay()));
        Response response = DDAPIUtil.access(GET_TEAM_LIST, requestItems, "get_team_list");
        if (!response.success()) {
            throw new DDAPIInvokeException("\n invoke push goods error(linkId:" + linkId + "):" +
                    "error_code:" + response.errorCode.getValue() + ",desc:" + response.desc);
        }
        Element element = response.data;
        if (response.errorCode == ErrorCode.SUCCESS && element != null) {
            Iterator ite = element.elementIterator("row"); // 获取
            while (ite.hasNext()) {
                Element itemEle = (Element) ite.next();
                String ddgid = itemEle.elementText("ddgid");
                String spgid = itemEle.elementText("spgid");
                if (linkId.toString().equals(spgid)) {
                    return ddgid;
                }
            }
        }
        return "";
    }
}
