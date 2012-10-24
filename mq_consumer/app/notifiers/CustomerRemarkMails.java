package notifiers;

import models.mail.MailMessage;
import play.Play;
import play.mvc.Mailer;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-10-24
 * Time: 上午10:58
 * To change this template use File | Settings | File Templates.
 */
public class CustomerRemarkMails extends Mailer {
    public static void notify(MailMessage message) {
        setSubject(message.getSubject());
        addRecipient(message.getRecipients().toArray(new String[message.getRecipients().size()]));
        setFrom(Play.configuration.getProperty("mail.smtp.user"));
        send(message);
    }
}
