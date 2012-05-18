package functional;

import models.MQTestConsumer;
import models.MQTestJournal;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.modules.rabbitmq.producer.RabbitMQPublisher;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author likang
 * Date: 12-5-18
 */
public class MQTest extends FunctionalTest{
    SimpleDateFormat format =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    @Before
    public void setup(){
        Fixtures.delete(MQTestJournal.class);
    }
    @Test
    public void consumeTest() throws InterruptedException {
        int count = 100;
        for(int i = 0; i< count; i++){
            RabbitMQPublisher.publish(MQTestConsumer.QUEUE, "test message" + i);
        }

        Thread.sleep(5000);
        List<MQTestJournal> journals = MQTestJournal.findAll();
        assertTrue(journals.size() > 0);
        Logger.info("first consumed time:" + format.format(journals.get(0).createdAt));
        Logger.info("last consumed time:" + format.format(journals.get(journals.size() - 1).createdAt));
        assertEquals(journals.size(), count);
    }
}
