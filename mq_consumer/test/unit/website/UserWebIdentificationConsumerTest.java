package unit.website;

import models.consumer.UserWebIdentification;
import models.consumer.UserWebIdentificationData;
import models.website.UserWebIdentificationConsumer;

import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.test.UnitTest;
import factory.FactoryBoy;

public class UserWebIdentificationConsumerTest extends UnitTest {
	
	private class UserWebIdentificationConsumerImpl extends UserWebIdentificationConsumer {
		public void doCousumer(UserWebIdentificationData data) {
			this.consume(data);
		}
	}

	private UserWebIdentificationConsumerImpl consumer;

	@Before
	public void setUp() {
		FactoryBoy.deleteAll();
		consumer = new UserWebIdentificationConsumerImpl();
	}
	
	@Test
	public void testConsumerObject() {
		assertEquals(0l, UserWebIdentification.count());
		UserWebIdentificationData data = new UserWebIdentificationData();
		data.cookieId = "didfa-33413adfa-313413-31ad";
		consumer.doCousumer(data);
		
		JPAPlugin.startTx(true);
		assertEquals(1l, UserWebIdentification.count());
		JPAPlugin.closeTx(true);
	}
}
