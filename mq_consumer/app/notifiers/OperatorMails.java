package notifiers;

import models.mail.MailMessage;
import play.mvc.Mailer;

/**
 * @author likang
 *         Date: 12-7-20
 *         Time: 下午5:14
 */
public class OperatorMails extends Mailer{
    public static void notify(MailMessage message) {
        setSubject(message.getSubject());
        addRecipient(message.getOneRecipient());
        setFrom("yibaiquan <noreplay@uhuila.com>");
        send(message);
    }
}
