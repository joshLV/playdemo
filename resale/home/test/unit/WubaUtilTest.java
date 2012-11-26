package unit;

import com.google.gson.JsonObject;
import models.wuba.WubaUtil;
import org.junit.Test;
import play.test.UnitTest;

/**
 * @author likang
 *         Date: 12-11-23
 */
public class WubaUtilTest extends UnitTest {
    @Test
    public void testCrypt() {
        String message = "中文1abcde中文2{++dsfds//???/f}";
        String result = "YzAwOTZiOWMyZGVjN2NhOTMzOGZmYzgzODNmNjE0Mzc1MjA3MDhkN2RmNDcwN2RiYzAwOTZiOWMyZGVjN2NhOTMzOGZmYzgzODNmNjE0Mzc1OGVkNzNkMDlhODhlMWYzYzQ3OWRiZTliOGI0M2VkNjk0ODVlNDUwMjc3MzFjNjk0ZDg0MmE3MGIxMTBhZjhmNmVlZmJjNjNlNTRhZTlkZA%3D%3D";
        String key = "312e6361abcf44199f206a204d432933";
        assertEquals(result, WubaUtil.encryptMessage(message, key));
        assertEquals(message, WubaUtil.decryptMessage(result, key));
    }

    @Test
    public void testParseResult() {
        String dataJson = "{" +
                "\"groupbuyId58\":123," +
                "\"groupbuyIdThirdpart\": 321" +
                "}";
        String json = "{" +
                "\"status\":10000," +
                "\"msg\": \"执行成功\"," +
                "\"data\": \"" + WubaUtil.encryptMessage(dataJson, WubaUtil.SECRET_KEY) + "\"" +
                "}";

        JsonObject result = WubaUtil.parseResponse(json);
        assertEquals(10000L, result.get("status").getAsLong());
        assertEquals("执行成功", result.get("msg").getAsString());
        JsonObject data = result.getAsJsonObject("data");
        assertEquals(123L, data.get("groupbuyId58").getAsLong());
        assertEquals(321L, data.get("groupbuyIdThirdpart").getAsLong());
    }

}
