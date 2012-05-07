package unit;

import static org.junit.Assert.assertEquals;
import models.sms.SmsSendConsumer;
import org.junit.Test;
import play.test.UnitTest;

public class SmsSendConsumerTest extends UnitTest {

    @Test
    public void testGenerateMd5Password() {
        SmsSendConsumer sms = new SmsSendConsumer();
        assertEquals("5a1a023fd486e2f0edbc595854c0d808", sms.generateMd5Password("wang", "qiqi", 1319873904));
    }

}
