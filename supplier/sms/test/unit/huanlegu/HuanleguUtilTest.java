package unit.huanlegu;

import models.huanlegu.HuanleguMessage;
import models.huanlegu.HuanleguUtil;
import org.junit.Test;
import play.mvc.Before;
import play.templates.Template;
import play.templates.TemplateLoader;
import play.test.UnitTest;
import util.ws.MockWebServiceClient;

import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-6-19
 */
public class HuanleguUtilTest extends UnitTest {
    @Before
    public void setup() {
        MockWebServiceClient.clear();
    }

    @Test
    public void testParse() throws Exception {
        String body = "<PageSize>3</PageSize>\n" +
                "<Sight>\n" +
                "    <SightId>1223</SightId>\n" +
                "    <SightName>景点名</SightName>\n" +
                "</Sight>";
        Template template = TemplateLoader.load("test/unit/huanlegu/sightInfoResponse.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("body", HuanleguUtil.encrypt(body));
        params.put("sign", HuanleguUtil.sign("123qwer", body));
        String response = template.render(params);

        HuanleguMessage message = HuanleguUtil.parseMessage(response, true);
        assertTrue(message.isResponseOk());
        assertEquals("1", message.version);
        assertEquals("3", message.selectTextTrim("./PageSize"));
        assertEquals("景点名", message.selectTextTrim("./Sight/SightName"));
    }

    @Test
    public void testRequest() throws Exception {
        String body = "<PageSize>3</PageSize>\n" +
                "<PageSize>4</PageSize>\n" +
                "<Sight>\n" +
                "    <SightId>1223</SightId>\n" +
                "    <SightName>景点名</SightName>\n" +
                "</Sight>";
        Template template = TemplateLoader.load("test/unit/huanlegu/sightInfoResponse.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("body", HuanleguUtil.encrypt(body));
        params.put("sign", HuanleguUtil.sign("123qwer", body));
        String response = template.render(params);

        MockWebServiceClient.addMockHttpRequest(200, response);

        params = new HashMap<>();
        params.put("sightId", "123456");
        params.put("sightName", "景点而已");
        HuanleguMessage message = HuanleguUtil.sendRequest("getSightInfo", params);

        assertTrue(message.isResponseOk());
        assertEquals("1", message.version);
        assertEquals("3", message.selectTextTrim("./PageSize"));
        assertEquals(2, message.selectNodes("./PageSize").size());
        assertEquals("景点名", message.selectTextTrim("./Sight/SightName"));
    }
    @Test
    public void testAny() {
        System.out.println(HuanleguUtil.sign("20130625171514864758", "<SightId>SH19910069</SightId><SightName>上海欢乐谷</SightName>"));
        System.out.println(HuanleguUtil.encrypt("<SightId>SH19910069</SightId><SightName>上海欢乐谷</SightName>"));
    }
}
