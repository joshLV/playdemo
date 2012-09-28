package notifiers;

import models.mail.MailMessage;
import play.Play;
import play.mvc.Mailer;

public class CouponMails extends Mailer {

    public static void notify(MailMessage message) {
        setSubject("[一百券] 您订购的消费券");
        addRecipient(message.getRecipients().toArray(new String[message.getRecipients().size()]));
        setFrom(Play.configuration.getProperty("mail.smtp.user"));
        send(message);
    }

}
