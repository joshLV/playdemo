package play.modules.emaillogs;

import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.net.SMTPAppender;
import play.Logger;
import play.Play;
import play.PlayPlugin;

/**
 * @author likang
 * Date: 12-7-27
 * 当运行在测试环境或者非生产环境时，删除邮件发送的Appender
 * 同时自定义 Appender 配置，使其允许从 Play! 的配置文件中读取配置
 */

public class EmailAppenderPlugin extends PlayPlugin{
    @Override
    public void onLoad(){
        if(Play.runingInTestMode() || !Play.mode.isProd()){
            Appender appender = org.apache.log4j.Logger.getRootLogger().getAppender("EMAIL");
            if(appender != null) {
                AsyncAppender asyncAppender = (AsyncAppender) appender;
                asyncAppender.removeAppender("GMAIL");
            }
        }
    }

    @Override
    public void onApplicationStart(){
        Appender appender = org.apache.log4j.Logger.getRootLogger().getAppender("EMAIL");
        if(appender == null) {
            return;
        }
        AsyncAppender asyncAppender = (AsyncAppender) appender;
        SMTPAppender smtpAppender = (SMTPAppender)asyncAppender.getAppender("GMAIL");
        if (smtpAppender == null) {
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

