package unit;

import models.dangdang.groupbuy.DDResponse;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import play.libs.XPath;
import play.test.UnitTest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

/**
 * @author likang
 *         Date: 13-1-22
 */
public class DDGroupBuyUtilTest extends UnitTest {
    @Test
    public void testParse() {
        String data =// "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>" +
                "<resultObject><status_code>0</status_code><error_code>0</error_code>" +
                "<desc><![CDATA[成功]]></desc><spid>3000003</spid><ver>1.0</ver>" +
                "<data><ddgid>256</ddgid><spgid>256</spgid><state>0</state></data></resultObject>";
        Document document;

        try{
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = builder.parse(new InputSource(new StringReader(data)));
        }catch (Exception e) {
            fail(); return;
        }
        DDResponse response = DDResponse.parseResponse(document);
        assertEquals("成功", response.desc);
        assertTrue(response.isSuccess());
        assertNotNull(response.data);
        assertEquals("256", XPath.selectText("//spgid", response.data));
    }
}
