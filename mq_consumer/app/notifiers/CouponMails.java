package notifiers;

import models.mail.CouponMessage;
import models.mail.MailMessage;
import play.mvc.Mailer;

public class CouponMails extends Mailer {

    public static void notify(MailMessage message) {

        setSubject("[优惠啦] 您订购优惠券");
        addRecipient(message.getEmail());
        setFrom("Uhuila <noreplay@uhuila.com>");
        send(message);
    }

}
