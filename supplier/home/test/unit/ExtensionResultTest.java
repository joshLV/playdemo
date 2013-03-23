package unit;

import cache.CacheHelper;
import org.junit.Test;
import play.test.UnitTest;
import util.extension.ExtensionResult;

/**
 * User: tanglq
 * Date: 13-3-23
 * Time: 下午8:43
 */
public class ExtensionResultTest extends UnitTest {
    @Test
    public void testCache() throws Exception {
        ExtensionResult result = ExtensionResult.SUCCESS;
        CacheHelper.setCache("ExtensionResultTest1", result);
        ExtensionResult cache = CacheHelper.getCache("ExtensionResultTest1");
        assertEquals(result.code, cache.code);
    }

    @Test
    public void testCacheFail() throws Exception {
        ExtensionResult result = ExtensionResult.INVALID_CALL;
        CacheHelper.setCache("ExtensionResultTest1", result);
        ExtensionResult cache = CacheHelper.getCache("ExtensionResultTest1");
        assertEquals(result.code, cache.code);
    }
}
