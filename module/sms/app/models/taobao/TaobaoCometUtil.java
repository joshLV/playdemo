package models.taobao;

import play.Play;
import play.modules.rabbitmq.producer.RabbitMQPublisher;

/**
 * @author likang
 * Date: 12-5-3
 */
public class TaobaoCometUtil {
    public static final String QUEUE_NAME = Play.mode.isProd() ? "taobao_comet" : "taobao_comet_dev";
    private TaobaoCometUtil(){}

    public static void send(String message){
        RabbitMQPublisher.publish(QUEUE_NAME, new TaobaoCometMessage(message));
    }
}
