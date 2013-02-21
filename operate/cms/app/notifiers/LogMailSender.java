package notifiers;

import play.Logger;
import play.Play;
import play.modules.emaillogs.EmailLogMessage;
import play.mvc.Mailer;

/**
 * User: tanglq
 * Date: 13-2-21
 * Time: 下午3:16
 */
public class LogMailSender extends Mailer {

    private static String LOG_RECEIVER = Play.configuration.getProperty("email_log.receiver", null);
    private static String LOG_SUBJECT = Play.configuration.getProperty("email_log.subject", null);
    private static String LOG_ENABLE = Play.configuration.getProperty("email_log.enable", "true");

    public static void sendLog(EmailLogMessage message) {
        if (!"true".equals(LOG_ENABLE)) {
            Logger.info("不发送异常邮件：【" + message.message + "】");
            // 不发送日志邮件
            return;
        }
        StringBuilder subject = new StringBuilder();
        subject.append("[").append(message.applicationName).append("]").append(LOG_SUBJECT);

        setSubject(subject.toString());
        addRecipient(LOG_RECEIVER);
        setFrom(Play.configuration.getProperty("mail.smtp.user"));
        send(message);
    }
}

