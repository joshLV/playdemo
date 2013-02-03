package models.dangdang.groupbuy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uhuila.common.util.DateUtil;
import models.order.ECoupon;
import models.order.OuterOrder;
import models.sales.ResalerProduct;
import org.apache.commons.codec.digest.DigestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import play.Logger;
import play.Play;
import play.libs.WS;
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
    private static final String VER = Play.configuration.getProperty("dangdang.groupbuy.version", "1.0");
    private static final String SECRET_KEY = Play.configuration.getProperty("dangdang.groupbuy.secret_key", "x8765d9yj72wevshn");
    private static final String SPID = Play.configuration.getProperty("dangdang.groupbuy.spid", "3000003");

    private static final String SYNC_URL = Play.configuration.getProperty("dangdang.sync_url", "http://tuanapi.dangdang.com/team_open/public/push_team_stock.php");
    private static final String QUERY_CONSUME_CODE_URL = Play.configuration.getProperty("dangdang.query_consume_code_url", "http://tuanapi.dangdang.com/team_open/public/query_consume_code.php");
    private static final String VERIFY_CONSUME_URL = Play.configuration.getProperty("dangdang.verify_consume_url", "http://tuanapi.dangdang.com/team_open/public/verify_consume.php");
    private static final String PUSH_PARTNER_TEAMS = Play.configuration.getProperty("dangdang.push_partner_teams", "http://tuanapi.dangdang.com/team_inter_api/public/push_partner_teams.php");
    private static final String GET_TEAM_LIST = Play.configuration.getProperty("dangdang.get_team_list", "http://tuanapi.dangdang.com/team_inter_api/public/get_team_list.php");

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
        for (Node node : XPath.selectNodes("//row", response.data)) {
            if (XPath.selectText("//spgid", node).equals(String.valueOf(linkId))) {
                return node;
            }
        }
        return null;
    }

    /**
     * 在当当上验证券.
     *
     * @param coupon 一百券自家券
     * @return 是否验证成功
     */
    public static boolean verifyOnDangdang(ECoupon coupon) {
        OuterOrder outerOrder = OuterOrder.find("byYbqOrder", coupon.order).first();
        if (outerOrder == null) {
            Logger.info("dangdang verifyOnDangDang failed: outerOrder not found; couponId: " + coupon.id);
            return false;
        }

        JsonObject jsonObject = new JsonParser().parse(outerOrder.message).getAsJsonObject();
        Map<String, Object> params = new HashMap<>();
        params.put("ddgid", jsonObject.get("team_id").getAsLong());
        params.put("consumeCode", coupon.eCouponSn);
        params.put("verifyCode", coupon.eCouponSn);

        String templatePath = "dangdang/groupbuy/verifyConsume.xml";
        String apiName = "verify_consume";
        DDResponse response = sendRequest(apiName, VERIFY_CONSUME_URL, templatePath, params);
        if (!response.isOk()) {
            Logger.info("verify on dangdang failed. couponId: %s; response: %s.", coupon.id, response);
        }
        return response.isOk();
    }

    /**
     * 将最新的销量同步到当当.
     *
     * @param product 分销产品
     * @return 同步是否成功
     */
    public static boolean syncSellCount(ResalerProduct product) {
        Map<String, Object> params = new HashMap<>();
        params.put("spgid", product.id);
        params.put("sellcount", product.goods.getRealSaleCount());

        String templatePath = "dangdang/groupbuy/getTeamList.xml";
        String apiName = "get_team_list";
        DDResponse response = sendRequest(apiName, SYNC_URL, templatePath, params);
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
        JsonObject jsonObject = new JsonParser().parse(outerOrder.message).getAsJsonObject();

        Map<String, Object> params = new HashMap<>();
        params.put("ddgid", jsonObject.get("team_id").getAsLong());
        params.put("type", 1);
        params.put("code", coupon.eCouponSn);

        String templatePath = "dangdang/groupbuy/queryConsumeCode.xml";
        String apiName = "query_consume_code";
        DDResponse response = sendRequest(apiName, QUERY_CONSUME_CODE_URL, templatePath, params);

        return Integer.parseInt(response.selectText("//state"));
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

        String templatePath = "dangdang/groupbuy/getTeamList.xml";
        String apiName = "get_team_list";
        return sendRequest(apiName, GET_TEAM_LIST, templatePath, params);
    }


    /**
     * 上传商品
     *
     * @param params 请求参数
     * @return 请求结果
     */
    public static DDResponse pushGoods(Map<String, Object> params) {
        String templatePath = "dangdang/groupbuy/pushGoods.xml";
        String apiName = "push_partner_teams";
        return sendRequest(apiName, PUSH_PARTNER_TEAMS, templatePath, params);
    }

    /**
     * 向当当发起请求
     *
     * @param url       请求的url
     * @param apiName   请求的api名称
     * @return 解析后的响应
     */
    public static DDResponse sendRequest(String apiName, String url, String templatePath, Map<String, Object> params,
                                         String ... keywords) {

        Template template = TemplateLoader.load(templatePath);
        String xmlData = template.render(params);

        Map<String, Object> requestParams = sysParams();
        requestParams.put("sign", sign(apiName, xmlData, (String) params.get("call_time")));
        requestParams.put("data", xmlData);

        Logger.info("dangdang request %s:\n%s", apiName, xmlData);
        WebServiceRequest request = WebServiceRequest.url(url).type("dangdang_" + apiName)
                .params(params);
        for (String keyword : keywords) {
            request.addKeyword(keyword);
        }

        Document xml = request.postXml();
        Logger.info("dangdang response %s:\n%s", apiName, xml.toString());

        return DDResponse.parseResponse(xml);
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

    public static String signParams(SortedMap<String, String> params) {
        StringBuilder signStr = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if ("body".equals(entry.getKey()) || "sign".equals(entry.getKey())) {
                continue;
            }
            signStr.append(entry.getKey()).append("=").append(WS.encode(entry.getValue())).append("&");
        }

        signStr.append("sn=").append(SECRET_KEY);
        return DigestUtils.md5Hex(signStr.toString());
    }
}
