package notifiers;

import models.mail.MailMessage;
import org.apache.commons.mail.EmailAttachment;
import play.mvc.Mailer;

/**
 * @author likang
 *         Date: 12-8-3
 */
public class MailSender extends Mailer{
    public static void send(MailMessage message) {
        setSubject(message.getSubject());
        addRecipient(message.getRecipients().toArray(new String[message.getRecipients().size()]));
        addBcc(message.getBccs().toArray(new String[message.getBccs().size()]));
        addCc(message.getCcs().toArray(new String[message.getCcs().size()]));
        addAttachment(message.getAttachments().toArray(
                new EmailAttachment[message.getAttachments().size()]));
        setFrom(message.getFrom());
        send("MailSender/" + message.getTemplate() + ".html", message);
    }
}
