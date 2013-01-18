package unit.sms;

import factory.FactoryBoy;
import models.journal.WebServiceCallLogData;
import models.sms.SMSException;
import models.sms.SMSMessage;
import models.sms.impl.BjenSMSProvider;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.ws.MockWebServiceClient;

public class BjenSMSProviderTest extends UnitTest {

    BjenSMSProvider provider = new BjenSMSProvider();
    
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        MockWebServiceClient.clear();
    }
    
    @Test
    public void testGenerateMd5Password() {
        BjenSMSProvider sms = new BjenSMSProvider();
        assertEquals("5a1a023fd486e2f0edbc595854c0d808", sms.generateMd5Password("wang", "qiqi", 1319873904));
    }

    @Test
    public void testSendMessageSuccess() {
        MockWebServiceClient.pushMockHttpRequest(200, "0");
        SMSMessage msg = new SMSMessage("Hello,world!", "15026682165");
        provider.send(msg);
        WebServiceCallLogData log = MockWebServiceClient.getLastWebServiceCallLog();
        assertEquals("ENSMS", log.callType);
        assertTrue(log.success);
    }
    

    @Test
    public void testSendMessageFail() {
        MockWebServiceClient.pushMockHttpRequest(200, "1");
        SMSMessage msg = new SMSMessage("Hello,world!", "15026682165");
        SMSException smsException = null;
        try {
            provider.send(msg);
        } catch (SMSException e) {
            smsException = e;
        }
        assertNotNull(smsException);
        WebServiceCallLogData log = MockWebServiceClient.getLastWebServiceCallLog();
        assertEquals("ENSMS", log.callType);
        assertTrue(log.success);
    }
}
