package unit;

import models.sms.impl.BjenSMSProvider;
import org.junit.Test;
import play.test.UnitTest;

public class BjenSMSProviderTest extends UnitTest {

    @Test
    public void testGenerateMd5Password() {
        BjenSMSProvider sms = new BjenSMSProvider();
        assertEquals("5a1a023fd486e2f0edbc595854c0d808", sms.generateMd5Password("wang", "qiqi", 1319873904));
    }

}
