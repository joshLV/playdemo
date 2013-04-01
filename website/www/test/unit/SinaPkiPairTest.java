package unit;

import models.payment.sina.SinaPkiPair;
import org.junit.Test;
import play.Logger;
import play.test.UnitTest;

/**
 * User: yan
 * Date: 13-3-28
 * Time: 下午4:17
 */
public class SinaPkiPairTest extends UnitTest {
    @Test
    public void testSignMsg() {
        String base64 = SinaPkiPair.signMsg("abc");
        Logger.info("signMsg: %s", base64);
    }
}
