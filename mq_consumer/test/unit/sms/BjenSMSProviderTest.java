package unit.sms;

import models.journal.WebServiceCallLog;
import models.journal.WebServiceCallType;
import models.sms.SMSException;
import models.sms.SMSMessage;
import models.sms.impl.BjenSMSProvider;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import util.ws.MockWebServiceClientHelper;
import factory.FactoryBoy;

public class BjenSMSProviderTest extends UnitTest {

    BjenSMSProvider provider = new BjenSMSProvider();
    
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        MockWebServiceClientHelper.clear();
    }
    
    @Test
    public void testGenerateMd5Password() {
        BjenSMSProvider sms = new BjenSMSProvider();
        assertEquals("5a1a023fd486e2f0edbc595854c0d808", sms.generateMd5Password("wang", "qiqi", 1319873904));
    }

    @Test
    public void testSendMessageSuccess() {
        MockWebServiceClientHelper.pushMockHttpRequest(200, "0");
        SMSMessage msg = new SMSMessage("Hello,world!", "15026682165");
        provider.send(msg);
        assertEquals(1, WebServiceCallType.count());
        assertEquals(1, WebServiceCallLog.count());
        WebServiceCallLog log = WebServiceCallLog.find("order by id desc").first();
        assertEquals("ENSMS", log.callType);
        assertTrue(log.success);
        WebServiceCallType type = WebServiceCallType.find("order by id desc").first();
        assertEquals("ENSMS", type.callType);
    }
    

    @Test
    public void testSendMessageFail() {
        MockWebServiceClientHelper.pushMockHttpRequest(200, "1");
        SMSMessage msg = new SMSMessage("Hello,world!", "15026682165");
        SMSException smsException = null;
        try {
            provider.send(msg);
        } catch (SMSException e) {
            smsException = e;
        }
        assertNotNull(smsException);
        assertEquals(1, WebServiceCallLog.count());
        WebServiceCallLog log = WebServiceCallLog.find("order by id desc").first();
        assertEquals("ENSMS", log.callType);
        assertTrue(log.success);
        WebServiceCallType type = WebServiceCallType.find("order by id desc").first();
        assertEquals("ENSMS", type.callType);
    }
}
