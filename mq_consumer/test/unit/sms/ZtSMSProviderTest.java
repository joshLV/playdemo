package unit.sms;

import models.sms.SMSMessage;
import models.sms.impl.ZtSMSProvider;

import org.junit.Ignore;
import org.junit.Test;

import play.test.UnitTest;

public class ZtSMSProviderTest extends UnitTest {
	
	@Ignore
	@Test
	public void testSendMessage() {
		SMSMessage message = new SMSMessage("Test Message", "18621736594");
		ZtSMSProvider provider = new ZtSMSProvider();
		provider.send(message);
	}
}
