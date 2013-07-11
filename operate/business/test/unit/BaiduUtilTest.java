package unit;

import com.google.gson.Gson;
import models.baidu.BaiduResponse;
import models.baidu.BaiduUtil;
import models.wuba.WubaUtil;
import org.junit.Ignore;
import org.junit.Test;
import play.Logger;
import play.Play;
import play.libs.WS;
import play.test.UnitTest;
import util.ws.PlayWebServiceClient;
import util.ws.WebServiceClient;
import util.ws.WebServiceClientFactory;
import util.ws.WebServiceRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * User: yan
 * Date: 13-7-11
 * Time: 下午5:04
 */
public class BaiduUtilTest extends UnitTest {

    @Test
    public void testParseResult_Success() {
        Play.id = "abc";
        Map<String, Object> appParams = new HashMap<>();
        appParams.put("province_id", "30");
        BaiduResponse response = BaiduUtil.sendRequest(appParams, "getCity.action");
        assertEquals("0", response.code);
        assertEquals("OK", response.msg);
    }

    @Test
    public void test_msg() {
        String json = "{\"msg\":\"错误情况\"," +
                "\"code\":400" + "}";
        BaiduResponse result = BaiduUtil.parseResponse(json);
        assertEquals("错误情况", result.msg);

    }

    @Test
    public void test_info() {
        String json = "{\"info\":\"错误情况\"," +
                "\"code\":400" + "}";
        BaiduResponse result = BaiduUtil.parseResponse(json);
        assertEquals("错误情况", result.msg);

    }

    private static Map<String, Object> sysParams() {
        Map<String, Object> paramMap = new HashMap<>();
        // 系统级参数设置（必须）
        paramMap.put("token", BaiduUtil.BAIDU_TOKEN);
        paramMap.put("userName", BaiduUtil.BAIDU_USER_NAME);

        return paramMap;
    }
}
