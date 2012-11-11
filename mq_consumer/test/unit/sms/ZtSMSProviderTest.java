package unit.sms;

import models.journal.WebServiceCallLog;
import models.journal.WebServiceCallType;
import models.sms.SMSException;
import models.sms.SMSMessage;
import models.sms.impl.BjenSMSProvider;
import models.sms.impl.ZtSMSProvider;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import factory.FactoryBoy;

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
        MockWebServiceClient.pushMockHttpRequest(200, "1,发送成功");
        SMSMessage msg = new SMSMessage("Hello,world!", "15026682165");
        provider.send(msg);
        assertEquals(1, WebServiceCallType.count());
        assertEquals(1, WebServiceCallLog.count());
        WebServiceCallLog log = MockWebServiceClient.getLastWebServiceCallLog();
        assertEquals("ZTSMS", log.callType);
        assertTrue(log.success);
        WebServiceCallType type = WebServiceCallType.find("order by id desc").first();
        assertEquals("ZTSMS", type.callType);
    }
    

    @Test
    public void testSendMessageFail() {
        MockWebServiceClient.pushMockHttpRequest(200, "3,发送失败");
        SMSMessage msg = new SMSMessage("Hello,world!", "15026682165");
        SMSException smsException = null;
        try {
            provider.send(msg);
        } catch (SMSException e) {
            smsException = e;
        }
        assertNotNull(smsException);
        assertEquals(1, WebServiceCallLog.count());
        WebServiceCallLog log = MockWebServiceClient.getLastWebServiceCallLog();
        assertEquals("ZTSMS", log.callType);
        assertTrue(log.success);
        WebServiceCallType type = WebServiceCallType.find("order by id desc").first();
        assertEquals("ZTSMS", type.callType);
    }
}
