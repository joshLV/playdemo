package play.modules.emaillogs;

import org.apache.log4j.Appender;
import org.apache.log4j.net.SMTPAppender;
import play.Logger;
import play.Play;
import play.PlayPlugin;

/**
 * @author likang
 * Date: 12-7-27
 */

public class EmailAppenderPlugin extends PlayPlugin{
    @Override
    public void onLoad(){
        Logger.info("loading EmailAppenderPlugin");
        if(Play.runingInTestMode() || !Play.mode.isProd()){
            Logger.info("remove log4j appender: EMAIL");
            org.apache.log4j.Logger.getRootLogger().removeAppender("EMAIL");
        }
    }

    @Override
    public void onApplicationStart(){
        Appender appender = org.apache.log4j.Logger.getRootLogger().getAppender("EMAIL");
        if(appender == null) {
            return;
        }

        String applicationName = Play.configuration.getProperty("application.name", null);
        String receiver = Play.configuration.getProperty("email_log.receiver", null);
        String host = Play.configuration.getProperty("email_log.host", null);
        String username = Play.configuration.getProperty("email_log.username", null);
        String password = Play.configuration.getProperty("email_log.password", null);
        String from = Play.configuration.getProperty("email_log.from", null);
        String subject = Play.configuration.getProperty("email_log.subject", null);
        String bufferSize = Play.configuration.getProperty("email_log.buffer_size", null);


        SMTPAppender smtpAppender = (SMTPAppender) appender;

        if (applicationName != null || subject != null) {
            String subjectPrefix =  applicationName == null ? "" : ("[" + applicationName + "] ");
            subject = subject == null ? smtpAppender.getSubject() : subject;
            smtpAppender.setSubject(subjectPrefix + subject);
        }

        if (receiver != null) {
            smtpAppender.setTo(receiver);
        }

        if (host != null) {
            smtpAppender.setSMTPHost(host);
        }

        if (username != null) {
            smtpAppender.setSMTPUsername(username);
        }

        if (password != null) {
            smtpAppender.setSMTPPassword(password);
        }

        if (from != null) {
            smtpAppender.setFrom(from);
        }

        if (bufferSize != null) {
            try{
                smtpAppender.setBufferSize(Integer.parseInt(bufferSize));
            }catch (NumberFormatException e) {
                //ignore
            }
        }
    }
}

