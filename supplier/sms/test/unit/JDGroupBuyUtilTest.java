package unit;

import models.jingdong.groupbuy.JDGroupBuyUtil;
import models.jingdong.groupbuy.JingdongMessage;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import play.libs.XML;
import play.libs.XPath;
import play.test.UnitTest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

/**
 * @author likang
 *         Date: 13-2-2
 */
public class JDGroupBuyUtilTest extends UnitTest {

    @Test
    public void testParseEncryptedMessage(){
        String messageXml = "<Message xmlns=\"http://tuan.jd.com/QueryCityResponse\"><Cities></Cities></Message>";
        String encryptedResponse = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<Response xmlns=\"http://tuan.jd.com/Response\">\n" +
                "    <Version>1.0</Version>\n" +
                "    <VenderId>1022</VenderId>\n" +
                "    <Zip>false</Zip>\n" +
                "    <Encrypt>true</Encrypt>\n" +
                "    <ResultCode>200</ResultCode>\n" +
                "    <ResultMessage>failure</ResultMessage>\n" +
                "    <Data>" + JDGroupBuyUtil.encryptMessage(messageXml) + "</Data>\n" +
                "</Response>";

        JingdongMessage message = JDGroupBuyUtil.parseMessage(encryptedResponse);
        assertTrue(message.isOk());
        assertTrue(message.encrypt);
        assertNotNull(message.message);
        assertNotNull(message.selectNode("./Cities"));
        assertNull(message.selectNode("./abc"));
    }

    @Test
    public void testParsePlainMessage() {
        String messageXml = "<Message xmlns=\"http://tuan.jd.com/QueryCityResponse\">" +
                "<Cities>" +
                "<City><Id>1</Id><Name>abc</Name></City>" +
                "<City><Id>2</Id><Name>abc</Name></City>" +
                "<City><Id>3</Id><Name>abc</Name></City>" +
                "</Cities></Message>";
        String plainResponse = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<Response xmlns=\"http://tuan.jd.com/Response\">\n" +
                "    <Version>1.0</Version>\n" +
                "    <VenderId>1022</VenderId>\n" +
                "    <Zip>false</Zip>\n" +
                "    <Encrypt>false</Encrypt>\n" +
                "    <ResultCode>200</ResultCode>\n" +
                "    <ResultMessage>failure</ResultMessage>\n" +
                "    <Data>" + messageXml + "</Data>\n" +
                "</Response>";

        JingdongMessage message = JDGroupBuyUtil.parseMessage(plainResponse);
        assertTrue(message.isOk());
        assertFalse(message.encrypt);
        assertNotNull(message.message);
        assertNotNull(message.selectNode("./Cities"));
        assertEquals(3, message.selectNodes("./Cities/City").size());
        assertNull(message.selectNode("./abc"));
    }
}
