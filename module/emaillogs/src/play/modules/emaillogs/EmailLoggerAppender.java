package play.modules.emaillogs;

import org.apache.log4j.net.SMTPAppender;
import org.apache.log4j.spi.LoggingEvent;
import play.Logger;
import play.Play;

/**
 * @author likang
 *         Date: 12-7-30
 *
 * 自定义 Appender 配置，使其允许从 Play! 的配置文件中读取配置
 */
public class EmailLoggerAppender extends SMTPAppender{

    @Override
    public void append(LoggingEvent event) {
        super.append(event);
    }
}
