package play.modules.emaillogs;

import org.apache.log4j.net.SMTPAppender;
import org.apache.log4j.spi.LoggingEvent;
import play.Logger;
import play.Play;

/**
 * @author likang
 *         Date: 12-7-30
 *
 * SMTPAppender的过滤器
 */
public class EmailLoggerAppender extends SMTPAppender{

    @Override
    public void append(LoggingEvent event) {
        super.append(event);
    }
}
