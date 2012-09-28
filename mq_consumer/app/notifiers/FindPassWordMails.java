package notifiers;

import models.mail.MailMessage;
import play.Play;
import play.mvc.Mailer;

public class FindPassWordMails extends Mailer {

    public static void notify(MailMessage message) {
        setSubject("[一百券] 找回密码");
        addRecipient(message.getRecipients().toArray(new String[message.getRecipients().size()]));
        setFrom(Play.configuration.getProperty("mail.smtp.user"));
        send(message);
    }
}
