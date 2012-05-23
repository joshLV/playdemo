package notifiers;

import models.mail.CouponMessage;
import models.mail.MailMessage;
import play.mvc.Mailer;

public class CouponMails extends Mailer {

    public static void notify(MailMessage message) {
        setSubject("[一百券] 您订购优惠券");
        addRecipient(message.getEmail());
        setFrom("yibaiquan <noreplay@uhuila.com>");
        send(message);
    }

}
