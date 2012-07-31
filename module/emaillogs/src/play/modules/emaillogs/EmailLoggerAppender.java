package play.modules.emaillogs;

import org.apache.log4j.net.SMTPAppender;
import play.Play;

/**
 * @author likang
 *         Date: 12-7-30
 */
public class EmailLoggerAppender extends SMTPAppender{
    static String receiver = Play.configuration.getProperty("email_log.receiver", "bugs@uhuila.com");
    static String applicationName = Play.configuration.getProperty("application.name", "");

    @Override
    public String getSubject(){
        return "[" + applicationName + "] " + super.getSubject();
    }

    @Override
    public String getTo(){
        return receiver;
    }
}
