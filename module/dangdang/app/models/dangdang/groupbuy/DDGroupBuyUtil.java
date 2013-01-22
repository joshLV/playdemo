package models.dangdang.groupbuy;

import org.apache.commons.codec.digest.DigestUtils;
import org.w3c.dom.Document;
import play.Play;
import play.templates.Template;
import play.templates.TemplateLoader;
import util.ws.WebServiceClient;
import util.ws.WebServiceClientFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
     * 上传商品
     *
     * @param params 请求参数
     * @return 请求结果
     */
    public static DDResponse pushGoods(Map<String, Object> params) {
        Template template = TemplateLoader.load("dangdang/groupbuy/pushGoods.xml");
        String xmlData = template.render(params);
        return sendRequest(PUSH_PARTNER_TEAMS, "push_partner_teams", xmlData);
    }

    /**
     * 向当当发起请求
     *
     * @param url       请求的url
     * @param apiName   请求的api名称
     * @param xmlData   xml形式的请求内容(不包含xml声明)
     * @return 解析后的响应
     */
    public static DDResponse sendRequest(String url, String apiName, String xmlData) {
        Map<String, Object> params = sysParams();
        params.put("sign", sign(apiName, xmlData, (String)params.get("call_time")));
        params.put("data", xmlData);

        WebServiceClient client = WebServiceClientFactory
                .getClientHelper();

        Document xml = client.postXml("dangdang_" + apiName,
                url, params, "dangdang");

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
        paramMap.put("call_time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) );
        return paramMap;
    }
}
