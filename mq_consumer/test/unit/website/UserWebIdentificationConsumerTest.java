package unit.website;

import factory.FactoryBoy;
import models.consumer.UserWebIdentification;
import models.consumer.UserWebIdentificationData;
import models.website.UserWebIdentificationConsumer;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

public class UserWebIdentificationConsumerTest extends UnitTest {

	private UserWebIdentificationConsumer consumer;

	@Before
	public void setUp() {
		FactoryBoy.deleteAll();
		consumer = new UserWebIdentificationConsumer();
	}
	
	@Test
	public void testConsumerObject() {
		assertEquals(0l, UserWebIdentification.count());
		UserWebIdentificationData data = new UserWebIdentificationData();
		data.cookieId = "didfa-33413adfa-313413-31ad";
		consumer.consumeWithTx(data);
		
		assertEquals(1l, UserWebIdentification.count());
	}
}
