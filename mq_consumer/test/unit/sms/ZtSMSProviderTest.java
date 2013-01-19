package unit.sms;

import factory.FactoryBoy;
import models.journal.WebServiceCallLogData;
import models.sms.SMSException;
import models.sms.SMSMessage;
import models.sms.impl.ZtSMSProvider;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.ws.MockWebServiceClient;

public class ZtSMSProviderTest extends UnitTest {

	ZtSMSProvider provider = new ZtSMSProvider();

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        MockWebServiceClient.clear();
    }

    @Test
    public void testSendMessageSuccess() {
        MockWebServiceClient.addMockHttpRequest(200, "1,发送成功");
        SMSMessage msg = new SMSMessage("Hello,world!", "15026682165");
        provider.send(msg);
        WebServiceCallLogData log = MockWebServiceClient.getLastWebServiceCallLog();
        assertEquals("ZTSMS", log.callType);
        assertTrue(log.success);
    }


    @Test
    public void testSendMessageFail() {
        MockWebServiceClient.addMockHttpRequest(200, "3,发送失败");
        SMSMessage msg = new SMSMessage("Hello,world!", "15026682165");
        SMSException smsException = null;
        try {
            provider.send(msg);
        } catch (SMSException e) {
            smsException = e;
        }
        assertNotNull(smsException);
        WebServiceCallLogData log = MockWebServiceClient.getLastWebServiceCallLog();
        assertEquals("ZTSMS", log.callType);
        assertTrue(log.success);
    }
}
