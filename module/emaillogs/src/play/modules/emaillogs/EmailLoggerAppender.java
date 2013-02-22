package play.modules.emaillogs;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import play.Play;
import play.modules.rabbitmq.producer.RabbitMQPublisher;

import java.net.InetAddress;

/**
 * @author likang
 *         Date: 12-7-30
 *         <p/>
 *         SMTPAppender的过滤器
 */
public class EmailLoggerAppender extends AppenderSkeleton {

    public static String hostIp;

    public static String applicationName;

    public boolean publishToMQ = false;  // 初始不发送，只有打开这个开关才会发送

    static {
        try {
            hostIp = InetAddress.getLocalHost().toString();
            applicationName = Play.configuration.getProperty("application.name", null);
        } catch (Exception e) {
            hostIp = "NotFoundIP";
        }
    }

    @Override
    public void append(LoggingEvent event) {
        String message = this.layout.format(event);
        EmailLogMessage emailLogMessage = EmailLogMessage.build().applicationName(applicationName).host(hostIp)
                .message(message);

        event.getLevel();

        if (publishToMQ && !Play.runingInTestMode()) {
            RabbitMQPublisher.publish(EmailLogMessage.MQ_KEY, emailLogMessage);
        }

    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return true;
    }
}
