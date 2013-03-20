package functional;

import models.weixin.WeixinUtil;
import org.junit.Test;
import org.w3c.dom.Document;
import play.libs.WS;
import play.libs.XML;
import play.libs.XPath;
import play.mvc.Http;
import play.templates.Template;
import play.templates.TemplateLoader;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-3-20
 */
public class WeixinAPITest extends FunctionalTest {
    @Test
    public void testHeartBeat() {
        String timestamp = String.valueOf((int)(new Date().getTime()/1000));
        String nonce = "abc";
        String echostr = "大山的子孙哟";
        String signature = WeixinUtil.makeSignature(timestamp, nonce);

        String url = "/api/v1/weixin/message?timestamp=" + timestamp
                + "&nonce=" + nonce
                + "&echostr=" + WS.encode(echostr)
                + "&signature=" + signature;
        Http.Response response = GET(url);
        assertIsOk(response);
        assertContentEquals(echostr, response);
    }

    @Test
    public void testBasicResponse() {
        String content = "this is a test";
        Template template = TemplateLoader.load("test/data/wx.basicTest.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("content", content);
        String requestBody = template.render(params);

        Http.Response response = POST("/api/v1/weixin/message", MULTIPART_FORM_DATA, requestBody);
        assertIsOk(response);
        String responseBody = getContent(response);
        Document responseXml = XML.getDocument(responseBody);
        assertEquals(content, XPath.selectText("/xml/Content", responseXml));
    }
}
