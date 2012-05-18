package models;

import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;
import play.modules.rabbitmq.producer.RabbitMQPublisher;

/**
 * @author  likang
 * Date: 12-5-18
 */
@OnApplicationStart(async = true)
public class MQTestConsumer extends RabbitMQConsumer<String>{
    public static final String QUEUE = "test_string_msg";


    @Override
    protected void consume(String message){
        new MQTestJournal(message).save();
    }

    @Override
    protected Class getMessageType() {
        return String.class;
    }

    @Override
    protected String queue() {
        return QUEUE;
    }
}
