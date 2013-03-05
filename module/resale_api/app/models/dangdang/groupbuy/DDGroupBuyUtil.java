package models.dangdang.groupbuy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uhuila.common.util.DateUtil;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.ECouponStatus;
import models.order.OuterOrder;
import models.sales.ResalerProduct;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import play.Logger;
import play.Play;
import play.libs.WS;
import play.libs.XML;
import play.libs.XPath;
import play.templates.Template;
import play.templates.TemplateLoader;
import util.ws.WebServiceRequest;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author likang
 *         Date: 13-1-22
 */
public class DDGroupBuyUtil {
    private static final String RESULT_FORMAT = "xml";
    private static final String SIGN_METHOD = "1";
    private static final String VER = Play.configuration.getProperty("dangdang.version", "1.0");
    private static final String SECRET_KEY = Play.configuration.getProperty("dangdang.secret_key", "x8765d9yj72wevshn");
    private static final String SPID = Play.configuration.getProperty("dangdang.spid", "3000003");

    /**
     * 查询刚刚今天上传的单个商品在当当上的信息.
     *
     * @param linkId 商品的linkId
     * @return 当当的商品信息
     */
    public static Node getJustUploadedTeam(Long linkId) {
        DDResponse response = getTeamList(DateUtil.getBeginOfDay(), DateUtil.getEndOfDay(),"10");
        if (!response.isOk() || response.data == null) {
            return null;
        }
        for (Node node : response.selectNodes("./row")) {
            if (XPath.selectText("./spgid", node).trim().equals(String.valueOf(linkId))) {
                return node;
            }
        }
        return null;
    }

    /**
     * 在当当上验证券.
     * 如果验证失败，则查询券信息，若已验证则返回验证成功.
     *
     * @param coupon 一百券自家券
     * @return 是否验证成功
     */
    public static boolean verifyOnDangdang(ECoupon coupon) {
        if (coupon.partner != ECouponPartner.DD) {
            return false;
        }
        OuterOrder outerOrder = OuterOrder.find("byYbqOrder", coupon.order).first();
        if (outerOrder == null) {
            Logger.info("verify on dangdang failed: outerOrder not found; couponId: " + coupon.id);
            return false;
        }

        JsonObject jsonObject = outerOrder.getMessageAsJsonObject();
        Map<String, Object> params = new HashMap<>();
        params.put("ddgid", jsonObject.get("team_id").getAsLong());
        params.put("consumeCode", coupon.eCouponSn);
        params.put("verifyCode", coupon.eCouponSn);

        DDResponse response = sendRequest("verify_consume", params);
        if (!response.isOk()) {
            Logger.info("verify on dangdang failed. couponId: %s; response: %s. Try to query coupon status", coupon.id, response);
            return couponStatus(coupon) == 1;
        }
        return true;
    }

    /**
     * 将最新的销量同步到当当.
     *
     * @param product 分销产品
     * @return 同步是否成功
     */
    public static boolean syncSellCount(ResalerProduct product) {
        Map<String, Object> params = new HashMap<>();
        params.put("spgid", product.goodsLinkId);
        params.put("sellcount", product.goods.getRealSaleCount());

        DDResponse response = sendRequest("push_team_stock", params);
        return response.isOk();
    }

    /**
     * 查询券在当当的状态.
     *
     * @param coupon 一百券自家的券
     * @return 券状态. 0: 未使用; 1: 已使用; 2: 已作废
     */
    public static int couponStatus(ECoupon coupon) {
        OuterOrder outerOrder = OuterOrder.find("byYbqOrder", coupon.order).first();
        if (outerOrder == null) {
            Logger.info("dangdang couponStatus failed: outerOrder not found; couponId: " + coupon.id);
            return -1;
        }
        JsonObject jsonObject = outerOrder.getMessageAsJsonObject();

        Map<String, Object> params = new HashMap<>();
        params.put("ddgid", jsonObject.get("team_id").getAsLong());
        params.put("type", 1);
        params.put("code", coupon.eCouponSn);

        DDResponse response = sendRequest("query_consume_code", params);
        if (!response.isOk()) {
            Logger.info("dangdang couponStatus failed: \n%s" + response);
            return -1;
        }

        return Integer.parseInt(response.selectTextTrim("./state"));
    }

    /**
     * 查询当当的项目列表
     *
     * @param startDate 项目录入的开始时间
     * @param endDate   项目录入的结束时间
     * @param status    项目状态. 为空时表示全部状态，多个状态用逗号隔开
     *                  0  : 等待处理
     *                  10 : 预处理完成，等待审核
     *                  11 : 预处理完成，但有问题，需要修改
     *                  100: 审核通过
     *                  110: 审核不通过，从未发布
     *                  20 : 审核通过后又有修改，等待处理
     * @return          请求结果
     */
    public static DDResponse getTeamList(Date startDate, Date endDate, String status) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", dateFormat.format(startDate));
        params.put("endDate", dateFormat.format(endDate));
        params.put("status", status);

        return sendRequest("get_team_list", params);
    }


    /**
     * 上传商品
     *
     * @param params 请求参数
     * @return 请求结果
     */
    public static DDResponse pushGoods(Map<String, Object> params) {
        return sendRequest("push_partner_teams", params);
    }

    /**
     * 向当当发起请求.
     *
     * 请保证 配置了 dangdang.url.[apiName]
     * 同时在 dangdang/groupbuy 包下有同名的xml模板文件
     *
     *
     * @param apiName   请求的api名称
     * @return 解析后的响应
     */
    public static DDResponse sendRequest(String apiName, Map<String, Object> params, String ... keywords) {
        String templatePath = "dangdang/groupbuy/" + apiName + ".xml";
        String url =Play.configuration.getProperty("dangdang.url." + apiName);

        Template template = TemplateLoader.load(templatePath);
        String xmlData = template.render(params);

        Map<String, Object> requestParams = sysParams();
        requestParams.put("sign", sign(apiName, xmlData, (String) requestParams.get("call_time")));
        requestParams.put("data", xmlData);

        Logger.info("dangdang request %s:\n%s", apiName, xmlData);
        WebServiceRequest request = WebServiceRequest.url(url).type("dangdang_" + apiName)
                .params(requestParams);
        for (String keyword : keywords) {
            request.addKeyword(keyword);
        }

        String xml = request.postString();
        Logger.info("dangdang response %s:\n%s", apiName, xml);

        return parseResponse(xml);
    }

    public static DDResponse parseResponse(String xml) {
        return parseResponse(XML.getDocument(xml));
    }

    public static DDResponse parseResponse(Document document) {
        DDResponse response = new DDResponse();
        response.ver = StringUtils.trimToNull(XPath.selectText("/resultObject/ver", document));
        response.spid = StringUtils.trimToNull(XPath.selectText("/resultObject/spid", document));
        response.errorCode = StringUtils.trimToNull(XPath.selectText("/resultObject/error_code", document));
        response.desc = StringUtils.trimToNull(XPath.selectText("/resultObject/desc", document));
        response.data = XPath.selectNode("/resultObject/data", document);
        return response;
    }

    /**
     * 获取加密签名
     *
     * @param apiName   api名称
     * @param data      请求内容
     * @param callTime  请求时间
     * @return  签名
     */
    public static String sign(String apiName, String data, String callTime) {
        return DigestUtils.md5Hex(SPID + apiName + VER + data + SECRET_KEY + callTime);
    }

    /**
     * 获取系统级参数.
     *
     * @return 默认的系统参数map
     */
    private static Map<String, Object> sysParams() {
        Map<String, Object> paramMap = new HashMap<>();
        // 系统级参数设置（必须）
        paramMap.put("spid", SPID);
        paramMap.put("result_format", RESULT_FORMAT);
        paramMap.put("ver", VER);
        paramMap.put("sign_method", SIGN_METHOD);
        paramMap.put("call_time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        return paramMap;
    }

    public static SortedMap<String, String> filterPlayParams(Map<String, String> params) {
        TreeMap<String, String> r = new TreeMap<>(params);
        r.remove("body");
        r.remove("format");
        return r;
    }

    /**
     * 此加密算法目前只用于 当当通知我们订单生成
     */
    public static String signParams(SortedMap<String, String> params) {
        StringBuilder signStr = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if ("body".equals(entry.getKey()) || "sign".equals(entry.getKey()) || "format".equals(entry.getKey()) ) {
                continue;
            }
            signStr.append(entry.getKey()).append("=").append(WS.encode(entry.getValue())).append("&");
        }

        signStr.append("sn=").append(SECRET_KEY);
        return DigestUtils.md5Hex(signStr.toString());
    }
}
