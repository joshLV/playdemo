package notifiers;

import models.mail.MailMessage;
import play.mvc.Mailer;

/**
 * @author likang
 *         Date: 12-7-27
 */
public class EmailLoggerMails extends Mailer{
    public static void notify(MailMessage message){
        setSubject(message.getSubject());
        setFrom(message.getFrom());
        addRecipient(message.getRecipients());
        send(message);
    }
}
