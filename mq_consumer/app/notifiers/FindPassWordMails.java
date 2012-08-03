package notifiers;

import models.mail.MailMessage;
import play.mvc.Mailer;

public class FindPassWordMails extends Mailer {

    public static void notify(MailMessage message) {
        setSubject("[一百券] 找回密码");
        addRecipient(message.getRecipients().toArray(new String[message.getRecipients().size()]));
        setFrom("yibaiquan <noreplay@uhuila.com>");
        send(message);
    }
}
