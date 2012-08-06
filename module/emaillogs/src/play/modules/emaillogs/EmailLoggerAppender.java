package play.modules.emaillogs;

import org.apache.log4j.net.SMTPAppender;
import play.Logger;
import play.Play;

/**
 * @author likang
 *         Date: 12-7-30
 *
 * 自定义 Appender 配置，使其允许从 Play! 的配置文件中读取配置
 */
public class EmailLoggerAppender extends SMTPAppender{
    static String applicationName = Play.configuration.getProperty("application.name", null);
    static String receiver = Play.configuration.getProperty("email_log.receiver", null);
    static String host = Play.configuration.getProperty("email_log.host", null);
    static String username = Play.configuration.getProperty("email_log.username", null);
    static String password = Play.configuration.getProperty("email_log.password", null);
    static String from = Play.configuration.getProperty("email_log.from", null);
    static String subject = Play.configuration.getProperty("email_log.subject", null);
    static String bufferSize = Play.configuration.getProperty("email_log.buffer_size", null);

    @Override
    public void setSubject(String s){
        Logger.info("set email logger subject. applicationName: %s, subject[conf]: %s, subject[param]: %s", applicationName, subject, s);
        String prefix = applicationName == null ? "" : "[" + applicationName + "] ";
        super.setSubject(prefix + (subject == null ? s : subject));
    }

    @Override
    public void setTo(String to){
        Logger.info("set email logger receiver. receiver[conf]: %s, receiver[param]: %s", receiver, to);
        super.setTo(receiver == null ? to : receiver);
    }

    @Override
    public void setSMTPHost(String h) {
        Logger.info("set email logger SMTPHost. host[conf]: %s, host[param]: %s", host, h);
        super.setSMTPHost(host == null ? h : host);
    }

    @Override
    public void setSMTPUsername(String u) {
        Logger.info("set email logger SMTPUser. user[conf]: %s, user[param]: %s", username, u);
        super.setSMTPUsername(username == null ? u : username);
    }

    @Override
    public void setSMTPPassword(String p) {
        Logger.info("set email logger SMTPPassword. password[conf]: %s, password[param]: %s", password, p);
        super.setSMTPPassword(password == null ? p : password);
    }

    @Override
    public void setFrom(String f) {
        Logger.info("set email logger From. from[conf]: %s, from[param]: %s", from, f);
        super.setFrom(from == null ? f : from);
    }

    @Override
    public void setBufferSize(int bs) {
        Logger.info("set email logger buffer. buffer[conf]: %s, buffer[param]: %s", bufferSize, bs);
        super.setBufferSize(bufferSize == null ? bs : Integer.parseInt(bufferSize));
    }
}
