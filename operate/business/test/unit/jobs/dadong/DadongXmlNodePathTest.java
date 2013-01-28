package unit.jobs.dadong;

import jobs.dadong.DadongXmlNodePath;
import org.junit.Test;
import play.test.UnitTest;

import java.util.List;

/**
 * User: tanglq
 * Date: 13-1-28
 * Time: 下午3:22
 */
public class DadongXmlNodePathTest extends UnitTest {

    @Test
    public void testSelectNodes() throws Exception {
        String xml = "<xml><a>123</a><a>456</a></xml>";
        List<String> nodes = DadongXmlNodePath.selectNodes("a", xml);
        for (String node : nodes) {
            System.out.println("node=" + node);
        }
        assertEquals(2, nodes.size());

        assertTrue(nodes.contains("123"));
        assertTrue(nodes.contains("456"));
    }

    @Test
    public void testSelectText() throws Exception {
        String xml = "<a>123</a><b>456</b>";
        assertEquals("123", DadongXmlNodePath.selectText("a", xml));
        assertEquals("456", DadongXmlNodePath.selectText("b", xml));
    }
}
